export type Order = 'asc' | 'desc';

export function stableSort<T>(
  array: T[],
  comparator: (a: T, b: T) => number
): T[] {
  const stabilizedThis = array.map((el, index) => [el, index] as [T, number]);
  stabilizedThis.sort((a, b) => {
    const order = comparator(a[0], b[0]);
    if (order !== 0) return order;
    return a[1] - b[1];
  });
  return stabilizedThis.map((el) => el[0]);
}

export function descendingComparator<T>(a: T, b: T, orderBy: keyof T): number {
  const aVal = a[orderBy];
  const bVal = b[orderBy];
  if (aVal === bVal) return 0;
  if (aVal === null || aVal === undefined) return 1;
  if (bVal === null || bVal === undefined) return -1;
  return aVal > bVal ? -1 : 1;
}

export function getComparator<Key extends keyof T, T>(
  order: Order,
  orderBy: Key
): (a: T, b: T) => number {
  return order === 'desc'
    ? (a, b) => descendingComparator(a, b, orderBy)
    : (a, b) => -descendingComparator(a, b, orderBy);
}