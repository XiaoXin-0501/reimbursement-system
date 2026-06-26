import type { BusinessTypeItem, BusinessTypeTreeNode } from '@/types/reference'

/**
 * Convert a flat business type array into a nested tree structure
 * based on the `superiorId` field.
 */
export function buildBusinessTypeTree(items: BusinessTypeItem[]): BusinessTypeTreeNode[] {
  const map = new Map<string, BusinessTypeTreeNode>()
  const roots: BusinessTypeTreeNode[] = []

  // First pass: create nodes
  for (const item of items) {
    map.set(item.businessTypeId, { ...item, children: [] })
  }

  // Second pass: build tree
  for (const item of items) {
    const node = map.get(item.businessTypeId)!
    if (item.superiorId === 'none' || !map.has(item.superiorId)) {
      roots.push(node)
    } else {
      const parent = map.get(item.superiorId)!
      if (!parent.children) parent.children = []
      parent.children!.push(node)
    }
  }

  // Clean up empty children arrays
  for (const node of map.values()) {
    if (node.children && node.children.length === 0) {
      delete node.children
    }
  }

  return roots
}

/**
 * Find a business type node by ID in a tree
 */
export function findBusinessTypeNode(
  tree: BusinessTypeTreeNode[],
  id: string,
): BusinessTypeTreeNode | null {
  for (const node of tree) {
    if (node.businessTypeId === id) return node
    if (node.children) {
      const found = findBusinessTypeNode(node.children, id)
      if (found) return found
    }
  }
  return null
}

/**
 * Get all leaf node IDs from a business type tree
 */
export function getLeafBusinessTypeIds(tree: BusinessTypeTreeNode[]): string[] {
  const ids: string[] = []
  for (const node of tree) {
    if (!node.children || node.children.length === 0) {
      ids.push(node.businessTypeId)
    } else {
      ids.push(...getLeafBusinessTypeIds(node.children))
    }
  }
  return ids
}
