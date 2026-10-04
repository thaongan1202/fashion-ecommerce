'use client';

import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Checkbox } from '@/components/ui/checkbox';
import { Label } from '@/components/ui/label';
import { Slider } from '@/components/ui/slider';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { Star, X, ChevronDown, ChevronUp } from 'lucide-react';
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from '@/components/ui/collapsible';

interface FilterOption {
  id: number | string;
  label: string;
  count?: number;
}

interface ProductFilterSidebarProps {
  // Filters
  categories?: FilterOption[];
  brands?: FilterOption[];
  
  // Selected values
  selectedCategoryId?: number;
  selectedBrandIds?: number[];
  selectedColorOptions?: string[];
  selectedSizeOptions?: string[];
  selectedMaterialOptions?: string[];
  priceRange?: [number, number];
  minRating?: number;
  inStockOnly?: boolean;
  onSaleOnly?: boolean;
  
  // Callbacks
  onCategoryChange?: (categoryId: number | undefined) => void;
  onBrandChange?: (brandIds: number[]) => void;
  onColorChange?: (colorOptions: string[]) => void;
  onSizeChange?: (sizeOptions: string[]) => void;
  onMaterialChange?: (materialOptions: string[]) => void;
  onPriceRangeChange?: (range: [number, number]) => void;
  onRatingChange?: (minRating: number | undefined) => void;
  onInStockOnlyChange?: (inStockOnly: boolean) => void;
  onOnSaleOnlyChange?: (onSaleOnly: boolean) => void;
  onClearFilters?: () => void;
}

// Predefined filter options
const COLOR_OPTIONS = ['Đen', 'Trắng', 'Be', 'Xanh navy', 'Xanh denim', 'Đỏ', 'Nâu'];
const SIZE_OPTIONS = ['S', 'M', 'L', '29', '30', '38', '39', '40', 'Freesize'];
const MATERIAL_OPTIONS = ['Cotton', 'Denim', 'Linen', 'Canvas', 'Da'];
const RATING_OPTIONS = [
  { id: '4.6+', value: 4.6, label: ' 4.6+' },
  { id: '4-4.5', value: 4.0, maxValue: 4.6, label: ' 4 - 4.5' },
];

const MIN_PRICE = 0;
const MAX_PRICE = 2000000;

export function ProductFilterSidebar({
  categories = [],
  brands = [],
  selectedCategoryId,
  selectedBrandIds = [],
  selectedColorOptions = [],
  selectedSizeOptions = [],
  selectedMaterialOptions = [],
  priceRange = [MIN_PRICE, MAX_PRICE],
  minRating,
  inStockOnly = false,
  onSaleOnly = false,
  onCategoryChange,
  onBrandChange,
  onColorChange,
  onSizeChange,
  onMaterialChange,
  onPriceRangeChange,
  onRatingChange,
  onInStockOnlyChange,
  onOnSaleOnlyChange,
  onClearFilters,
}: ProductFilterSidebarProps) {
  // Collapsible state
  const [openSections, setOpenSections] = useState({
    category: true,
    brand: true,
    price: true,
    specs: true,
    rating: true,
    other: true,
  });

  const toggleSection = (section: keyof typeof openSections) => {
    setOpenSections(prev => ({ ...prev, [section]: !prev[section] }));
  };

  // Format price - VND chuẩn cho slider
  const formatSliderPrice = (price: number) => {
    if (price >= 1000000) {
      return (price / 1000000).toFixed(1) + ' triệu';
    }
    return price.toLocaleString('vi-VN') + '₫';
  };

  // Check if any filter is active
  const hasActiveFilters = 
    selectedCategoryId ||
    selectedBrandIds.length > 0 ||
    selectedColorOptions.length > 0 ||
    selectedSizeOptions.length > 0 ||
    selectedMaterialOptions.length > 0 ||
    priceRange[0] !== MIN_PRICE ||
    priceRange[1] !== MAX_PRICE ||
    minRating !== undefined ||
    inStockOnly ||
    onSaleOnly;

  // Toggle brand
  const handleBrandToggle = (brandId: number) => {
    const newBrandIds = selectedBrandIds.includes(brandId)
      ? selectedBrandIds.filter(id => id !== brandId)
      : [...selectedBrandIds, brandId];
    onBrandChange?.(newBrandIds);
  };

  const handleColorToggle = (color: string) => {
    const next = selectedColorOptions.includes(color)
      ? selectedColorOptions.filter(item => item !== color)
      : [...selectedColorOptions, color];
    onColorChange?.(next);
  };

  const handleSizeToggle = (size: string) => {
    const next = selectedSizeOptions.includes(size)
      ? selectedSizeOptions.filter(item => item !== size)
      : [...selectedSizeOptions, size];
    onSizeChange?.(next);
  };

  const handleMaterialToggle = (material: string) => {
    const next = selectedMaterialOptions.includes(material)
      ? selectedMaterialOptions.filter(item => item !== material)
      : [...selectedMaterialOptions, material];
    onMaterialChange?.(next);
  };

  return (
    <div className="space-y-4">
      {/* Header with Clear Filters */}
      <Card>
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <CardTitle className="text-lg">Bộ lọc</CardTitle>
            {hasActiveFilters && (
              <Button
                variant="ghost"
                size="sm"
                onClick={onClearFilters}
                className="h-8 px-2"
              >
                <X className="w-4 h-4 mr-1" />
                Xóa bộ lọc
              </Button>
            )}
          </div>
        </CardHeader>
      </Card>

      {/* Category Filter */}
      {categories.length > 0 && (
        <Card>
          <Collapsible open={openSections.category} onOpenChange={() => toggleSection('category')}>
            <CardHeader className="pb-3">
              <CollapsibleTrigger className="flex items-center justify-between w-full">
                <CardTitle className="text-base">Danh mục</CardTitle>
                {openSections.category ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
              </CollapsibleTrigger>
            </CardHeader>
            <CollapsibleContent>
              <CardContent className="space-y-2">
                <div
                  className={`flex items-center justify-between p-2 rounded cursor-pointer hover:bg-accent ${
                    !selectedCategoryId ? 'bg-accent' : ''
                  }`}
                  onClick={() => onCategoryChange?.(undefined)}
                >
                  <span className="text-sm">Tất cả</span>
                </div>
                {categories.map((category) => (
                  <div
                    key={category.id}
                    className={`flex items-center justify-between p-2 rounded cursor-pointer hover:bg-accent ${
                      selectedCategoryId === category.id ? 'bg-accent' : ''
                    }`}
                    onClick={() => onCategoryChange?.(Number(category.id))}
                  >
                    <span className="text-sm">{category.label}</span>
                  </div>
                ))}
              </CardContent>
            </CollapsibleContent>
          </Collapsible>
        </Card>
      )}

      {/* Brand Filter */}
      {brands.length > 0 && (
        <Card>
          <Collapsible open={openSections.brand} onOpenChange={() => toggleSection('brand')}>
            <CardHeader className="pb-3">
              <CollapsibleTrigger className="flex items-center justify-between w-full">
                <CardTitle className="text-base">Thương hiệu</CardTitle>
                {openSections.brand ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
              </CollapsibleTrigger>
            </CardHeader>
            <CollapsibleContent>
              <CardContent className="space-y-3">
                {brands.map((brand) => (
                  <div key={brand.id} className="flex items-center space-x-2">
                    <Checkbox
                      id={`brand-${brand.id}`}
                      checked={selectedBrandIds.includes(Number(brand.id))}
                      onCheckedChange={() => handleBrandToggle(Number(brand.id))}
                    />
                    <Label
                      htmlFor={`brand-${brand.id}`}
                      className="text-sm font-normal cursor-pointer flex-1"
                    >
                      {brand.label}
                    </Label>
                  </div>
                ))}
              </CardContent>
            </CollapsibleContent>
          </Collapsible>
        </Card>
      )}

      {/* Price Range Filter */}
      <Card>
        <Collapsible open={openSections.price} onOpenChange={() => toggleSection('price')}>
          <CardHeader className="pb-3">
            <CollapsibleTrigger className="flex items-center justify-between w-full">
              <CardTitle className="text-base">Khoảng giá</CardTitle>
              {openSections.price ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
            </CollapsibleTrigger>
          </CardHeader>
          <CollapsibleContent>
            <CardContent className="space-y-4">
              <Slider
                min={MIN_PRICE}
                max={MAX_PRICE}
                step={50000}
                value={priceRange}
                onValueChange={(value: number[]) => onPriceRangeChange?.(value as [number, number])}
                className="w-full"
              />
              <div className="flex items-center justify-between text-sm">
                <span className="font-medium">{formatSliderPrice(priceRange[0])}</span>
                <span className="text-muted-foreground">-</span>
                <span className="font-medium">{formatSliderPrice(priceRange[1])}</span>
              </div>
            </CardContent>
          </CollapsibleContent>
        </Collapsible>
      </Card>

      {/* Specifications Filter */}
      <Card>
        <Collapsible open={openSections.specs} onOpenChange={() => toggleSection('specs')}>
          <CardHeader className="pb-3">
            <CollapsibleTrigger className="flex items-center justify-between w-full">
              <CardTitle className="text-base">Thuộc tính</CardTitle>
              {openSections.specs ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
            </CollapsibleTrigger>
          </CardHeader>
          <CollapsibleContent>
            <CardContent className="space-y-4">
              <div>
                <h4 className="text-sm font-medium mb-2">Màu sắc</h4>
                <div className="flex flex-wrap gap-2">
                  {COLOR_OPTIONS.map((color) => (
                    <Badge
                      key={color}
                      variant={selectedColorOptions.includes(color) ? 'default' : 'outline'}
                      className="cursor-pointer"
                      onClick={() => handleColorToggle(color)}
                    >
                      {color}
                    </Badge>
                  ))}
                </div>
              </div>

              <Separator />

              <div>
                <h4 className="text-sm font-medium mb-2">Kích thước</h4>
                <div className="flex flex-wrap gap-2">
                  {SIZE_OPTIONS.map((size) => (
                    <Badge
                      key={size}
                      variant={selectedSizeOptions.includes(size) ? 'default' : 'outline'}
                      className="cursor-pointer"
                      onClick={() => handleSizeToggle(size)}
                    >
                      {size}
                    </Badge>
                  ))}
                </div>
              </div>

              <Separator />

              <div>
                <h4 className="text-sm font-medium mb-2">Chất liệu</h4>
                <div className="flex flex-wrap gap-2">
                  {MATERIAL_OPTIONS.map((material) => (
                    <Badge
                      key={material}
                      variant={selectedMaterialOptions.includes(material) ? 'default' : 'outline'}
                      className="cursor-pointer"
                      onClick={() => handleMaterialToggle(material)}
                    >
                      {material}
                    </Badge>
                  ))}
                </div>
              </div>
            </CardContent>
          </CollapsibleContent>
        </Collapsible>
      </Card>

      {/* Rating Filter */}
      <Card>
        <Collapsible open={openSections.rating} onOpenChange={() => toggleSection('rating')}>
          <CardHeader className="pb-3">
            <CollapsibleTrigger className="flex items-center justify-between w-full">
              <CardTitle className="text-base">Đánh giá</CardTitle>
              {openSections.rating ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
            </CollapsibleTrigger>
          </CardHeader>
          <CollapsibleContent>
            <CardContent className="space-y-2">
              <div
                className={`flex items-center gap-2 p-2 rounded cursor-pointer hover:bg-accent ${
                  !minRating ? 'bg-accent' : ''
                }`}
                onClick={() => onRatingChange?.(undefined)}
              >
                <span className="text-sm">Tất cả</span>
              </div>
              {RATING_OPTIONS.map((option) => (
                <div
                  key={option.value}
                  className={`flex items-center gap-2 p-2 rounded cursor-pointer hover:bg-accent ${
                    minRating === option.value ? 'bg-accent' : ''
                  }`}
                  onClick={() => onRatingChange?.(option.value)}
                >
                  <Star className="w-4 h-4 fill-yellow-400 text-yellow-400" />
                  <span className="text-sm">{option.label}</span>
                </div>
              ))}
            </CardContent>
          </CollapsibleContent>
        </Collapsible>
      </Card>

      {/* Other Filters */}
      <Card>
        <Collapsible open={openSections.other} onOpenChange={() => toggleSection('other')}>
          <CardHeader className="pb-3">
            <CollapsibleTrigger className="flex items-center justify-between w-full">
              <CardTitle className="text-base">Khác</CardTitle>
              {openSections.other ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
            </CollapsibleTrigger>
          </CardHeader>
          <CollapsibleContent>
            <CardContent className="space-y-3">
              <div className="flex items-center space-x-2">
                <Checkbox
                  id="in-stock"
                  checked={inStockOnly}
                  onCheckedChange={(checked: boolean) => onInStockOnlyChange?.(!!checked)}
                />
                <Label htmlFor="in-stock" className="text-sm font-normal cursor-pointer">
                  Chỉ sản phẩm còn hàng
                </Label>
              </div>
              <div className="flex items-center space-x-2">
                <Checkbox
                  id="on-sale"
                  checked={onSaleOnly}
                  onCheckedChange={(checked: boolean) => onOnSaleOnlyChange?.(!!checked)}
                />
                <Label htmlFor="on-sale" className="text-sm font-normal cursor-pointer">
                  Đang khuyến mãi
                </Label>
              </div>
            </CardContent>
          </CollapsibleContent>
        </Collapsible>
      </Card>
    </div>
  );
}
