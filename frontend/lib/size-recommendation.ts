const CLOTHING_SIZES = [
  { size: "XS", height: 152, weight: 45 },
  { size: "S", height: 160, weight: 52 },
  { size: "M", height: 168, weight: 60 },
  { size: "L", height: 175, weight: 70 },
  { size: "XL", height: 182, weight: 80 },
  { size: "XXL", height: 188, weight: 92 },
  { size: "2XL", height: 188, weight: 92 },
  { size: "3XL", height: 192, weight: 105 },
];

function normalizeSize(value: string) {
  return value.trim().toUpperCase().replace(/\s+/g, "");
}

export function isShoeProduct(categoryName?: string | null, sizes: string[] = []) {
  const name = (categoryName || "").toLowerCase();
  if (/giày|giay|dép|dep|sandal|boot|shoe/.test(name)) return true;
  const numeric = sizes.filter((size) => /^\d{2}$/.test(size.trim()));
  return numeric.length > 0 && numeric.every((size) => Number(size) >= 34 && Number(size) <= 48);
}

export function isApparelProduct(categoryName?: string | null, sizes: string[] = []) {
  if (isShoeProduct(categoryName, sizes)) return false;
  const name = (categoryName || "").toLowerCase();
  if (/áo|ao|quần|quan|váy|vay|đầm|dam|set|hoodie|jacket|shirt|pant|dress|thời trang/.test(name)) {
    return true;
  }
  return sizes.some((size) => /^(xs|s|m|l|xl|xxl|2xl|3xl)$/i.test(size.trim()));
}

export function suggestSize(input: {
  categoryName?: string | null;
  heightCm: number;
  weightKg: number;
  availableSizes: string[];
}): string | null {
  const { categoryName, heightCm, weightKg, availableSizes } = input;
  if (!availableSizes.length || heightCm < 100 || heightCm > 220 || weightKg < 30 || weightKg > 200) {
    return null;
  }

  if (isShoeProduct(categoryName, availableSizes)) {
    let eu = Math.round(36 + (heightCm - 150) * 0.2);
    if (weightKg >= 80) eu += 1;
    if (weightKg <= 45) eu -= 1;
    eu = Math.min(46, Math.max(35, eu));
    const exact = availableSizes.find((size) => Number.parseInt(size, 10) === eu);
    if (exact) return exact;
    const numeric = availableSizes
      .map((size) => ({ size, value: Number.parseInt(size, 10) }))
      .filter((item) => !Number.isNaN(item.value));
    numeric.sort((a, b) => Math.abs(a.value - eu) - Math.abs(b.value - eu));
    return numeric[0]?.size ?? null;
  }

  if (!isApparelProduct(categoryName, availableSizes)) return null;

  const ranked = CLOTHING_SIZES.map((item) => ({
    ...item,
    score: Math.abs(item.height - heightCm) * 0.7 + Math.abs(item.weight - weightKg) * 1.1,
  })).sort((a, b) => a.score - b.score);

  for (const candidate of ranked) {
    const match = availableSizes.find((size) => normalizeSize(size) === candidate.size);
    if (match) return match;
  }
  return availableSizes[0] ?? null;
}
