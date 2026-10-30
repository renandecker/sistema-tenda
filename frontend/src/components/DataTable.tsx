import React, { useState, useMemo } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  TableSortLabel,
  Paper,
  InputBase,
  Chip,
  Select,
  MenuItem,
  FormControl,
  Skeleton,
  Box,
} from '@mui/material';
import {
  Search as SearchIcon,
  ArrowDownward as ArrowDownIcon,
  ArrowUpward as ArrowUpIcon,
} from '@mui/icons-material';
import { Order, stableSort } from '@/utils/tableHelpers';
import { formatCurrency, formatDate, formatDateTime, formatNumber, getStatusColor, getStatusLabel } from '@/utils/formatters';

interface Column<T> {
  field: keyof T | string;
  headerName: string;
  width?: number;
  flex?: number;
  render?: (value: any, row: T) => React.ReactNode;
  sortable?: boolean;
  align?: 'left' | 'center' | 'right';
  type?: 'text' | 'number' | 'currency' | 'date' | 'datetime' | 'status' | 'boolean';
}

interface DataTableProps<T> {
  data: T[];
  columns: Column<T>[];
  loading?: boolean;
  pagination?: boolean;
  /** When true, `data` already contains only the current page (fetched from the server). */
  serverSide?: boolean;
  /** Total number of rows across all pages. Used for the pagination footer when `serverSide` is true. */
  totalCount?: number;
  page?: number;
  rowsPerPage?: number;
  rowsPerPageOptions?: number[];
  onPageChange?: (page: number, rowsPerPage: number) => void;
  onSortChange?: (field: string, direction: Order) => void;
  onRowClick?: (row: T) => void;
  searchable?: boolean;
  searchPlaceholder?: string;
  filterable?: boolean;
  filterOptions?: { field: string; label: string; options: { value: string; label: string }[] }[];
  filterValues?: Record<string, string>;
  onFilterChange?: (filters: Record<string, string>) => void;
  selectable?: boolean;
  selectedRows?: T[];
  onSelectionChange?: (rows: T[]) => void;
  getRowId?: (row: T) => string | number;
  emptyMessage?: string;
  stickyHeader?: boolean;
  maxHeight?: number;
}

export function DataTable<T extends Record<string, any>>({
  data,
  columns,
  loading = false,
  pagination = true,
  serverSide = false,
  totalCount,
  page = 0,
  rowsPerPage = 10,
  rowsPerPageOptions = [10, 20, 50, 100],
  onPageChange,
  onSortChange,
  onRowClick,
  searchable = true,
  searchPlaceholder = 'Buscar...',
  filterable = false,
  filterOptions = [],
  filterValues = {},
  onFilterChange,
  selectable = false,
  selectedRows = [],
  onSelectionChange,
  getRowId = (row: T) => (row as any).id,
  emptyMessage = 'Nenhum registro encontrado',
  stickyHeader = true,
  maxHeight = 600,
}: DataTableProps<T>) {
  const [sortConfig, setSortConfig] = useState<{ field: string; direction: Order }>({ field: '', direction: 'asc' });
  const [searchTerm, setSearchTerm] = useState('');

  const handleSort = (field: string) => {
    const direction = sortConfig.field === field && sortConfig.direction === 'asc' ? 'desc' : 'asc';
    setSortConfig({ field, direction });
    onSortChange?.(field, direction);
  };

  const filteredData = useMemo(() => {
    let result = [...data];
    
    if (searchTerm) {
      const term = searchTerm.toLowerCase();
      result = result.filter(row => 
        columns.some(col => {
          const value = row[col.field as keyof T];
          return String(value ?? '').toLowerCase().includes(term);
        })
      );
    }

    Object.entries(filterValues).forEach(([field, value]) => {
      if (value) {
        result = result.filter(row => String(row[field] ?? '').toLowerCase().includes(value.toLowerCase()));
      }
    });

    if (sortConfig.field) {
      result = stableSort(result, (a, b) => {
        const aVal = a[sortConfig.field as keyof T];
        const bVal = b[sortConfig.field as keyof T];
        if (aVal === bVal) return 0;
        if (aVal === null || aVal === undefined) return 1;
        if (bVal === null || bVal === undefined) return -1;
        return sortConfig.direction === 'asc' 
          ? (aVal > bVal ? 1 : -1) 
          : (aVal < bVal ? 1 : -1);
      });
    }

    return result;
  }, [data, searchTerm, filterValues, sortConfig, columns]);

  const paginatedData = serverSide
    ? filteredData
    : (pagination ? filteredData.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage) : filteredData);

  const totalRows = serverSide ? (totalCount ?? filteredData.length) : filteredData.length;

  const isRowSelected = (row: T) => selectedRows.some(s => getRowId(s) === getRowId(row));

  const handleSelectAll = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.checked) {
      onSelectionChange?.(filteredData);
    } else {
      onSelectionChange?.([]);
    }
  };

  const handleSelectRow = (row: T) => {
    const rowId = getRowId(row);
    if (isRowSelected(row)) {
      onSelectionChange?.(selectedRows.filter(r => getRowId(r) !== rowId));
    } else {
      onSelectionChange?.([...selectedRows, row]);
    }
  };

  const renderCell = (row: T, column: Column<T>) => {
    const value = row[column.field as keyof T];
    
    if (column.render) {
      return column.render(value, row);
    }

    switch (column.type) {
      case 'currency':
        return <span>{formatCurrency(value)}</span>;
      case 'number':
        return <span style={{ textAlign: column.align || 'right' }}>{formatNumber(value)}</span>;
      case 'date':
        return <span>{formatDate(value)}</span>;
      case 'datetime':
        return <span>{formatDateTime(value)}</span>;
      case 'status':
        return <Chip label={getStatusLabel(String(value))} size="small" color={getStatusColor(String(value))} />;
      case 'boolean':
        return value ? 'Sim' : 'Não';
      default:
        return <span>{value ?? '-'}</span>;
    }
  };

  if (loading) {
    return (
      <TableContainer sx={{ maxHeight }}>
        <Table stickyHeader={stickyHeader}>
          <TableHead>
            <TableRow>
              {columns.map((col) => (
                <TableCell key={String(col.field)} style={{ width: col.width, flex: col.flex }}>
                  <Skeleton variant="text" width={col.headerName.length * 8} />
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {[...Array(5)].map((_, i) => (
              <TableRow key={i}>
                {columns.map((col) => (
                  <TableCell key={String(col.field)}>
                    <Skeleton variant="text" width="80%" />
                  </TableCell>
                ))}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    );
  }

  return (
    <Paper sx={{ overflow: 'hidden' }}>
      {(searchable || filterable) && (
        <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider', display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
          {searchable && (
            <Box sx={{ flexGrow: 1, minWidth: 200, position: 'relative' }}>
              <SearchIcon sx={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'text.secondary' }} />
              <InputBase
                placeholder={searchPlaceholder}
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                sx={{ width: '100%', pl: 40, pr: 2, py: 1, borderRadius: 1, border: 1, borderColor: 'divider', '&:hover': { borderColor: 'primary.main' }, '&.Mui-focused': { borderColor: 'primary.main' } }}
              />
            </Box>
          )}
          {filterable && filterOptions.length > 0 && (
            <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
              {filterOptions.map((filter) => (
                <FormControl key={filter.field} size="small" sx={{ minWidth: 180 }}>
                  <Select
                    value={filterValues[filter.field] || ''}
                    onChange={(e) => onFilterChange?.({ ...filterValues, [filter.field]: e.target.value })}
                    displayEmpty
                    sx={{ width: '100%' }}
                  >
                    <MenuItem value="">{filter.label}</MenuItem>
                    {filter.options.map((opt) => (
                      <MenuItem key={opt.value} value={opt.value}>{opt.label}</MenuItem>
                    ))}
                  </Select>
                </FormControl>
              ))}
            </Box>
          )}
        </Box>
      )}

      <TableContainer sx={{ maxHeight }}>
        <Table stickyHeader={stickyHeader}>
          <TableHead>
            <TableRow>
              {selectable && (
                <TableCell padding="checkbox">
                  <input type="checkbox" checked={selectedRows.length === filteredData.length && filteredData.length > 0} onChange={handleSelectAll} />
                </TableCell>
              )}
              {columns.map((col) => (
                <TableCell
                  key={String(col.field)}
                  style={{ width: col.width, flex: col.flex, textAlign: col.align || 'left', cursor: col.sortable ? 'pointer' : 'default' }}
                  align={col.align}
                >
                  {col.sortable ? (
                    <TableSortLabel
                      active={sortConfig.field === col.field}
                      direction={sortConfig.field === col.field ? sortConfig.direction : 'asc'}
                      onClick={() => handleSort(String(col.field))}
                    >
                      {col.headerName}
                      {sortConfig.field === col.field && (
                        sortConfig.direction === 'asc' ? <ArrowUpIcon fontSize="small" /> : <ArrowDownIcon fontSize="small" />
                      )}
                    </TableSortLabel>
                  ) : (
                    col.headerName
                  )}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredData.length === 0 ? (
              <TableRow>
                <TableCell colSpan={columns.length + (selectable ? 1 : 0)} align="center" sx={{ py: 4 }}>
                  {emptyMessage}
                </TableCell>
              </TableRow>
            ) : (
              paginatedData.map((row, index) => (
                <TableRow
                  key={getRowId(row) ?? index}
                  hover={!!onRowClick}
                  onClick={() => onRowClick?.(row)}
                  selected={isRowSelected(row)}
                  sx={{ '&:last-child td': { border: 0 } }}
                >
                  {selectable && (
                    <TableCell padding="checkbox">
                      <input type="checkbox" checked={isRowSelected(row)} onChange={() => handleSelectRow(row)} />
                    </TableCell>
                  )}
                  {columns.map((col) => (
                    <TableCell
                      key={String(col.field)}
                      align={col.align}
                      sx={{ textAlign: col.align || 'left' }}
                    >
                      {renderCell(row, col)}
                    </TableCell>
                  ))}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {pagination && (
        <TablePagination
          rowsPerPageOptions={rowsPerPageOptions}
          component="div"
          count={totalRows}
          page={page}
          rowsPerPage={rowsPerPage}
          onPageChange={(_, newPage) => onPageChange?.(newPage, rowsPerPage)}
          onRowsPerPageChange={(e) => onPageChange?.(0, parseInt(e.target.value, 10))}
        />
      )}
    </Paper>
  );
}