import { defineStore } from 'pinia'
import { ref } from 'vue'
import { encryptObject, decryptObject } from '@/utils/crypto'
import type { ReimbursementListRow } from '@/types/reimbursement'
import { ReimStatus } from '@/types/common'

const DRAFT_INDEX_KEY = 'sy_reim_draft_index'
const DRAFT_PREFIX = 'sy_reim_draft_'
const MAX_DRAFTS = 5
const MAX_AGE_DAYS = 7

interface DraftMeta {
  key: string
  title: string
  createdAt: string // ISO timestamp
}

export const useDraftStore = defineStore('draft', () => {
  const draftMetas = ref<DraftMeta[]>([])

  /** Load all draft metadata from the index (decrypt index if needed) */
  function loadDraftMetas(): DraftMeta[] {
    try {
      const raw = localStorage.getItem(DRAFT_INDEX_KEY)
      if (!raw) return []
      const metas = JSON.parse(raw) as DraftMeta[]
      // Filter expired
      const now = Date.now()
      const valid = metas.filter((m) => {
        const age = (now - new Date(m.createdAt).getTime()) / (1000 * 60 * 60 * 24)
        return age <= MAX_AGE_DAYS
      })
      // If some expired, clean up
      if (valid.length < metas.length) {
        cleanExpired()
      }
      return valid
    } catch {
      return []
    }
  }

  /** Save draft metadata index */
  function saveDraftMetas(metas: DraftMeta[]): void {
    localStorage.setItem(DRAFT_INDEX_KEY, JSON.stringify(metas))
  }

  /** Save a draft to localStorage. Returns true if saved, false if limit reached. */
  function saveDraft(data: ReimbursementListRow & { trips?: unknown[]; subsidies?: unknown[]; expenses?: unknown[]; allocations?: unknown[]; remarks?: string }): string | null {
    const metas = loadDraftMetas()

    // Update existing or create new key
    let key = data.draftKey
    const isNewKey = !key
    if (!key) {
      key = DRAFT_PREFIX + Date.now()
    }

    // Enforce max drafts — do NOT auto-evict. Reject new drafts beyond limit.
    if (isNewKey && !metas.find((m) => m.key === key) && metas.length >= MAX_DRAFTS) {
      return null
    }

    // Encrypt and save
    const encrypted = encryptObject(data)
    localStorage.setItem(key, encrypted)

    // Update metadata index
    const existingIdx = metas.findIndex((m) => m.key === key)
    const meta: DraftMeta = {
      key,
      title: data.title || '未命名报销单',
      createdAt: existingIdx >= 0 ? metas[existingIdx]!.createdAt : new Date().toISOString(),
    }
    if (existingIdx >= 0) {
      metas[existingIdx] = meta
    } else {
      metas.push(meta)
    }
    saveDraftMetas(metas)
    draftMetas.value = metas

    return key
  }

  /** Load a single draft by key */
  function loadDraft(key: string): ReimbursementListRow | null {
    try {
      const raw = localStorage.getItem(key)
      if (!raw) return null
      return decryptObject<ReimbursementListRow>(raw)
    } catch {
      return null
    }
  }

  /** Delete a single draft by key */
  function deleteDraft(key: string): void {
    localStorage.removeItem(key)
    const metas = loadDraftMetas().filter((m) => m.key !== key)
    saveDraftMetas(metas)
    draftMetas.value = metas
  }

  /** Load all current drafts as list rows */
  function loadAllDrafts(): ReimbursementListRow[] {
    const metas = loadDraftMetas()
    draftMetas.value = metas
    const drafts: ReimbursementListRow[] = []
    for (const meta of metas) {
      const draft = loadDraft(meta.key)
      if (draft) {
        drafts.push({
          ...draft,
          status: ReimStatus.DRAFT,
          isDraft: true,
          draftKey: meta.key,
          isLocal: true,
        })
      }
    }
    return drafts
  }

  /** Count current valid drafts */
  function draftCount(): number {
    return loadDraftMetas().length
  }

  /** Check if new draft can be created (below max) */
  function canCreateDraft(): boolean {
    return draftCount() < MAX_DRAFTS
  }

  /** Clean expired drafts */
  function cleanExpired(): void {
    const metas = loadDraftMetas()
    const now = Date.now()
    const expired = metas.filter((m) => {
      const age = (now - new Date(m.createdAt).getTime()) / (1000 * 60 * 60 * 24)
      return age > MAX_AGE_DAYS
    })
    for (const m of expired) {
      localStorage.removeItem(m.key)
    }
    const valid = metas.filter((m) => !expired.includes(m))
    saveDraftMetas(valid)
    draftMetas.value = valid
  }

  return {
    MAX_DRAFTS,
    draftMetas,
    loadDraftMetas,
    saveDraft,
    loadDraft,
    deleteDraft,
    loadAllDrafts,
    draftCount,
    canCreateDraft,
    cleanExpired,
  }
})
