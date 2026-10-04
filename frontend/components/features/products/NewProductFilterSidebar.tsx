'use client';

import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Checkbox } from '@/components/ui/checkbox';
import { Label } from '@/components/ui/label';
import { Slider } from '@/components/ui/slider';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { Star, X, ChevronDown, ChevronUp, Filter } from 'lucide-react';
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from '@/components/ui/collapsible';
import type { ProductFilterRequest } from '@/services/new-product.service';

interface FilterOption {
  id: number | string;
  label: string;
  count?: number;
  children?: FilterOption[];
}

interface NewProductFilterSidebarProps {
  categories?: FilterOption[];
  brands?: FilterOption[];

  // Current filter values
  currentFilters: ProductFilterRequest;

  // Callbacks
  onFiltersChange: (filters: ProductFilterRequest) => void;
  onApplyFilters?: () => void; // Optional when autoApply is true
  onClearFilters: () => void;

  // Loading state
  isLoading?: boolean;

  // Auto-apply mode - filters apply immediately on change
  autoApply?: boolean;
}

const COLOR_OPTIONS = ['Đen', 'Trắng', 'Xanh navy', 'Be', 'Xám', 'Xanh denim'];
const SIZE_OPTIONS = ['S', 'M', 'L', 'XL', 'XXL', '38', '39', '40', '41', '42'];
const MATERIAL_OPTIONS = ['Cotton', 'Denim', 'Kaki', 'Linen', 'Canvas', 'Da'];
const STYLE_OPTIONS = ['Basic', 'Công sở', 'Đường phố', 'Thể thao', 'Tối giản'];
const AUDIENCE_OPTIONS = ['Nam', 'Nữ', 'Unisex'];

export function NewProductFilterSidebar({
  categories = [],
  brands = [],
  currentFilters,
  onFiltersChange,
  onApplyFilters,
  onClearFilters,
  isLoading = false,
  autoApply = false,
}: NewProductFilterSidebarProps) {
  // Collapsible sections state
  const [openSections, setOpenSections] = useState<Record<string, boolean>>({
    categories: true,
    brands: true,
    price: true,
    rating: true,
    specs: false,
    status: true,
  });

  const toggleSection = (section: string) => {
    setOpenSections(prev => ({ ...prev, [section]: !prev[section] }));
  };

  // Price range helpers - format cho slider
  const formatSliderPrice = (price: number) => {
    if (price >= 1000000) {
      return `${(price / 1000000).toFixed(1)} triệu`;
    }
    if (price >= 1000) {
      return `${(price / 1000).toFixed(0)}K`;
    }
    return price.toString();
  };

  const getPriceRange = (): [number, number] => [
    currentFilters.minPrice || 0,
    currentFilters.maxPrice || 50000000
  ];

  const setPriceRange = (range: [number, number]) => {
    onFiltersChange({
      ...currentFilters,
      minPrice: range[0] > 0 ? range[0] : undefined,
      maxPrice: range[1] < 50000000 ? range[1] : undefined,
    });
  };

  // Category handlers
  const handleCategoryChange = (categoryId: number, checked: boolean) => {
    const currentIds = currentFilters.categoryIds || [];
    const newIds = checked
      ? [...currentIds, categoryId]
      : currentIds.filter(id => id !== categoryId);

    onFiltersChange({
      ...currentFilters,
      categoryIds: newIds.length > 0 ? newIds : undefined,
    });
  };

  // Brand handlers  
  const handleBrandChange = (brandId: number, checked: boolean) => {
    const currentIds = currentFilters.brandIds || [];
    const newIds = checked
      ? [...currentIds, brandId]
      : currentIds.filter(id => id !== brandId);

    onFiltersChange({
      ...currentFilters,
      brandIds: newIds.length > 0 ? newIds : undefined,
    });
  };

  // Spec handlers
  const toggleOption = (
    key: 'colorOptions' | 'sizeOptions' | 'materialOptions' | 'styleOptions' | 'targetAudienceOptions',
    value: string,
    checked: boolean,
  ) => {
    const currentOptions = currentFilters[key] || [];
    const newOptions = checked
      ? [...currentOptions, value]
      : currentOptions.filter(option => option !== value);

    onFiltersChange({
      ...currentFilters,
      [key]: newOptions.length > 0 ? newOptions : undefined,
    });
  };

  // Rating range options: 4-4.5, 4.6+
  type RatingOption = { id: string; label: string; minRating: number; maxRating?: number };
  const RATING_OPTIONS: RatingOption[] = [
    { id: '4.6+', label: '4.6+', minRating: 4.6 },
    { id: '4-4.5', label: '4 - 4.5', minRating: 4.0, maxRating: 4.6 },
  ];

  // Get current rating option id based on filters
  const getCurrentRatingOption = (): string | undefined => {
    const { minRating, maxRating } = currentFilters;
    if (minRating === 4.6 && !maxRating) return '4.6+';
    if (minRating === 4.0 && maxRating === 4.6) return '4-4.5';
    return undefined;
  };

  // Rating handler
  const handleRatingChange = (optionId: string | undefined) => {
    if (!optionId) {
      onFiltersChange({
        ...currentFilters,
        minRating: undefined,
        maxRating: undefined,
      });
      return;
    }
    const option = RATING_OPTIONS.find(o => o.id === optionId);
    if (option) {
      onFiltersChange({
        ...currentFilters,
        minRating: option.minRating,
        maxRating: option.maxRating,
      });
    }
  };

  // Count active filters
  const getActiveFiltersCount = () => {
    let count = 0;
    if (currentFilters.categoryIds?.length) count += currentFilters.categoryIds.length;
    if (currentFilters.brandIds?.length) count += currentFilters.brandIds.length;
    if (currentFilters.minPrice || currentFilters.maxPrice) count += 1;
    if (currentFilters.colorOptions?.length) count += currentFilters.colorOptions.length;
    if (currentFilters.sizeOptions?.length) count += currentFilters.sizeOptions.length;
    if (currentFilters.materialOptions?.length) count += currentFilters.materialOptions.length;
    if (currentFilters.styleOptions?.length) count += currentFilters.styleOptions.length;
    if (currentFilters.targetAudienceOptions?.length) count += currentFilters.targetAudienceOptions.length;
    if (currentFilters.minRating || currentFilters.maxRating) count += 1;
    if (currentFilters.inStockOnly) count += 1;
    if (currentFilters.hasDiscountOnly) count += 1;
    return count;
  };

  const activeFiltersCount = getActiveFiltersCount();

  return (
    <Card className="sticky top-6">
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="flex items-center gap-2 text-lg">
            <Filter className="w-5 h-5" />
            Bộ lọc
            {activeFiltersCount > 0 && (
              <Badge variant="secondary" className="ml-2">
                {activeFiltersCount}
              </Badge>
            )}
          </CardTitle>
          {activeFiltersCount > 0 && (
            <Button
              variant="ghost"
              size="sm"
              onClick={onClearFilters}
              className="text-xs"
            >
              <X className="w-3 h-3 mr-1" />
              Xóa tất cả
            </Button>
          )}
        </div>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* Categories */}
        {categories.length > 0 && (
          <div>
            <Collapsible open={openSections.categories} onOpenChange={() => toggleSection('categories')}>
              <CollapsibleTrigger className="flex items-center justify-between w-full p-2 hover:bg-muted rounded">
                <span className="font-medium">Danh mục</span>
                {openSections.categories ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
              </CollapsibleTrigger>
              <CollapsibleContent className="mt-2 space-y-2">
                {categories.map((category) => (
                  <div key={category.id}>
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id={`category-${category.id}`}
                        checked={currentFilters.categoryIds?.includes(Number(category.id)) || false}
                        onCheckedChange={(checked) => handleCategoryChange(Number(category.id), !!checked)}
                      />
                      <Label
                        htmlFor={`category-${category.id}`}
                        className="flex-1 text-sm font-normal cursor-pointer"
                      >
                        {category.label}
                      </Label>
                    </div>
                    {/* Sub-categories */}
                    {category.children && category.children.length > 0 && (
                      <div className="ml-6 mt-1 space-y-1 border-l border-border pl-2">
                        {category.children.map((child) => (
                          <div key={child.id} className="flex items-center space-x-2">
                            <Checkbox
                              id={`category-${child.id}`}
                              checked={currentFilters.categoryIds?.includes(Number(child.id)) || false}
                              onCheckedChange={(checked) => handleCategoryChange(Number(child.id), !!checked)}
                            />
                            <Label
                              htmlFor={`category-${child.id}`}
                              className="flex-1 text-xs font-normal cursor-pointer text-muted-foreground"
                            >
                              {child.label}
                            </Label>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                ))}
              </CollapsibleContent>
            </Collapsible>
            <Separator className="mt-4" />
          </div>
        )}

        {/* Brands */}
        {brands.length > 0 && (
          <div>
            <Collapsible open={openSections.brands} onOpenChange={() => toggleSection('brands')}>
              <CollapsibleTrigger className="flex items-center justify-between w-full p-2 hover:bg-muted rounded">
                <span className="font-medium">Thương hiệu</span>
                {openSections.brands ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
              </CollapsibleTrigger>
              <CollapsibleContent className="mt-2 space-y-2 max-h-48 overflow-y-auto">
                {brands.map((brand) => (
                  <div key={brand.id} className="flex items-center space-x-2">
                    <Checkbox
                      id={`brand-${brand.id}`}
                      checked={currentFilters.brandIds?.includes(Number(brand.id)) || false}
                      onCheckedChange={(checked) => handleBrandChange(Number(brand.id), !!checked)}
                    />
                    <Label
                      htmlFor={`brand-${brand.id}`}
                      className="flex-1 text-sm font-normal cursor-pointer"
                    >
                      {brand.label}
                    </Label>
                  </div>
                ))}
              </CollapsibleContent>
            </Collapsible>
            <Separator className="mt-4" />
          </div>
        )}

        {/* Price Range */}
        <div>
          <Collapsible open={openSections.price} onOpenChange={() => toggleSection('price')}>
            <CollapsibleTrigger className="flex items-center justify-between w-full p-2 hover:bg-muted rounded">
              <span className="font-medium">Khoảng giá</span>
              {openSections.price ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
            </CollapsibleTrigger>
            <CollapsibleContent className="mt-2 space-y-4">
              <div className="px-2">
                <Slider
                  value={getPriceRange()}
                  onValueChange={setPriceRange}
                  max={50000000}
                  min={0}
                  step={1000000}
                  className="w-full"
                />
                <div className="flex items-center justify-between text-sm text-muted-foreground mt-2">
                  <span>{formatSliderPrice(getPriceRange()[0])}</span>
                  <span>{formatSliderPrice(getPriceRange()[1])}</span>
                </div>
              </div>
            </CollapsibleContent>
          </Collapsible>
          <Separator className="mt-4" />
        </div>

        {/* Rating */}
        <div>
          <Collapsible open={openSections.rating} onOpenChange={() => toggleSection('rating')}>
            <CollapsibleTrigger className="flex items-center justify-between w-full p-2 hover:bg-muted rounded">
              <span className="font-medium">Đánh giá</span>
              {openSections.rating ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
            </CollapsibleTrigger>
            <CollapsibleContent className="mt-2 space-y-2">
              {RATING_OPTIONS.map((option) => (
                <div key={option.id} className="flex items-center space-x-2">
                  <Checkbox
                    id={`rating-${option.id}`}
                    checked={getCurrentRatingOption() === option.id}
                    onCheckedChange={(checked) => handleRatingChange(checked ? option.id : undefined)}
                  />
                  <Label htmlFor={`rating-${option.id}`} className="flex items-center space-x-2 cursor-pointer">
                    <div className="flex items-center gap-1">
                      <Star className="w-4 h-4 fill-yellow-400 text-yellow-400" />
                      <span className="text-sm font-medium">{option.label}</span>
                    </div>
                  </Label>
                </div>
              ))}
            </CollapsibleContent>
          </Collapsible>
          <Separator className="mt-4" />
        </div>

        <div>
          <Collapsible open={openSections.specs} onOpenChange={() => toggleSection('specs')}>
            <CollapsibleTrigger className="flex items-center justify-between w-full p-2 hover:bg-muted rounded">
              <span className="font-medium">Thuộc tính</span>
              {openSections.specs ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
            </CollapsibleTrigger>
            <CollapsibleContent className="mt-2 space-y-4">
              {[
                { label: 'Màu sắc', key: 'colorOptions' as const, options: COLOR_OPTIONS },
                { label: 'Kích thước', key: 'sizeOptions' as const, options: SIZE_OPTIONS },
                { label: 'Chất liệu', key: 'materialOptions' as const, options: MATERIAL_OPTIONS },
                { label: 'Phong cách', key: 'styleOptions' as const, options: STYLE_OPTIONS },
                { label: 'Đối tượng', key: 'targetAudienceOptions' as const, options: AUDIENCE_OPTIONS },
              ].map((group) => (
                <div key={group.key}>
                  <Label className="text-sm font-medium mb-2 block">{group.label}</Label>
                  <div className="grid grid-cols-2 gap-2">
                    {group.options.map((option) => (
                      <div key={option} className="flex items-center space-x-1">
                        <Checkbox
                          id={`${group.key}-${option}`}
                          checked={currentFilters[group.key]?.includes(option) || false}
                          onCheckedChange={(checked) => toggleOption(group.key, option, !!checked)}
                        />
                        <Label htmlFor={`${group.key}-${option}`} className="text-xs cursor-pointer">
                          {option}
                        </Label>
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </CollapsibleContent>
          </Collapsible>
          <Separator className="mt-4" />
        </div>

        {/* Status Filters */}
        <div>
          <Collapsible open={openSections.status} onOpenChange={() => toggleSection('status')}>
            <CollapsibleTrigger className="flex items-center justify-between w-full p-2 hover:bg-muted rounded">
              <span className="font-medium">Trạng thái</span>
              {openSections.status ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
            </CollapsibleTrigger>
            <CollapsibleContent className="mt-2 space-y-2">
              <div className="flex items-center space-x-2">
                <Checkbox
                  id="in-stock"
                  checked={currentFilters.inStockOnly || false}
                  onCheckedChange={(checked) => onFiltersChange({
                    ...currentFilters,
                    inStockOnly: !!checked || undefined,
                  })}
                />
                <Label htmlFor="in-stock" className="text-sm cursor-pointer">
                  Chỉ sản phẩm còn hàng
                </Label>
              </div>
              <div className="flex items-center space-x-2">
                <Checkbox
                  id="on-sale"
                  checked={currentFilters.hasDiscountOnly || false}
                  onCheckedChange={(checked) => onFiltersChange({
                    ...currentFilters,
                    hasDiscountOnly: !!checked || undefined,
                  })}
                />
                <Label htmlFor="on-sale" className="text-sm cursor-pointer">
                  Chỉ sản phẩm giảm giá
                </Label>
              </div>
            </CollapsibleContent>
          </Collapsible>
        </div>

        {/* Apply Filters Button - Only shown when not in autoApply mode */}
        {!autoApply && onApplyFilters && (
          <Button
            onClick={onApplyFilters}
            className="w-full"
            disabled={isLoading}
          >
            {isLoading ? 'Đang tải...' : 'Áp dụng bộ lọc'}
          </Button>
        )}

        {/* Loading indicator for auto-apply mode */}
        {autoApply && isLoading && (
          <div className="flex items-center justify-center py-2 text-sm text-muted-foreground">
            <div className="w-4 h-4 border-2 border-primary border-t-transparent rounded-full animate-spin mr-2" />
            Đang lọc...
          </div>
        )}
      </CardContent>
    </Card>
  );
}