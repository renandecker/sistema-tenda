interface DecimalParts {
  intPart: string;
  decPart: string | null;
}

function splitDecimalParts(raw: string, maxDecimals: number): DecimalParts {
  if (raw.includes(',')) {
    const lastComma = raw.lastIndexOf(',');
    return {
      intPart: raw.slice(0, lastComma).replace(/\D/g, ''),
      decPart: raw.slice(lastComma + 1).replace(/\D/g, ''),
    };
  }
  const lastDot = raw.lastIndexOf('.');
  if (lastDot >= 0) {
    const after = raw.slice(lastDot + 1);
    const singleDot = raw.indexOf('.') === lastDot;
    if (singleDot && after.length > 0 && after.length <= maxDecimals && /^\d+$/.test(after)) {
      return { intPart: raw.slice(0, lastDot).replace(/\D/g, ''), decPart: after };
    }
  }
  return { intPart: raw.replace(/\D/g, ''), decPart: null };
}

export function maskDecimal(value: string | number | null | undefined, maxDecimals = 2): string {
  if (value === null || value === undefined) return '';
  const raw = typeof value === 'number' ? (Number.isFinite(value) ? String(value) : '') : String(value).trim();
  if (!raw) return '';
  const { intPart, decPart } = splitDecimalParts(raw, maxDecimals);
  if (decPart === null || maxDecimals <= 0) return intPart;
  const dec = decPart.slice(0, maxDecimals);
  if (!intPart && !dec) return '';
  return `${intPart},${dec}`;
}

export function maskCurrency(value: string | number | null | undefined): string {
  return maskDecimal(value, 2);
}

export function maskQuantity(value: string | number | null | undefined): string {
  return maskDecimal(value, 3);
}

export function maskInteger(value: string | number | null | undefined): string {
  return maskDecimal(value, 0);
}

export function parseDecimal(value: string | number | null | undefined, maxDecimals = 2): number {
  if (typeof value === 'number') return Number.isFinite(value) ? value : 0;
  if (value === null || value === undefined) return 0;
  const raw = String(value).trim();
  if (!raw) return 0;
  const { intPart, decPart } = splitDecimalParts(raw, maxDecimals);
  const num = Number(intPart || 0) + Number(decPart ? `0.${decPart.slice(0, maxDecimals)}` : 0);
  return Number.isFinite(num) ? num : 0;
}

export function parseCurrency(value: string | number | null | undefined): number {
  return parseDecimal(value, 2);
}

export function maskCpfCnpj(value: string | number | null | undefined): string {
  if (value === null || value === undefined) return '';
  const d = String(value).replace(/\D/g, '').slice(0, 14);
  if (!d) return '';
  if (d.length <= 11) {
    const parts = [d.slice(0, 3), d.slice(3, 6), d.slice(6, 9)].filter(Boolean);
    let out = parts.join('.');
    if (d.length > 9) out += `-${d.slice(9, 11)}`;
    return out;
  }
  const parts = [d.slice(0, 2), d.slice(2, 5), d.slice(5, 8)].filter(Boolean);
  let out = parts.join('.');
  if (d.length > 8) out += `/${d.slice(8, 12)}`;
  if (d.length > 12) out += `-${d.slice(12, 14)}`;
  return out;
}

export function parseDocument(value: string | number | null | undefined): string {
  if (value === null || value === undefined) return '';
  return String(value).replace(/\D/g, '');
}
