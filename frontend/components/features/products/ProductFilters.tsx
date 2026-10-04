'use client'

import React, { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import { Slider } from '@/components/ui/slider'
import { Badge } from '@/components/ui/badge'
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible'
import { Separator } from '@/components/ui/separator'
import { X, Star, Shirt } from 'lucide-react'
import { cn } from '@/lib/utils'
import type { Brand } from '@/types/brand'
import type { Category } from '@/types/category'

interface FilterState {
  keyword?: string
  categoryIds: number[]
  brandIds: number[]
  minPrice?: number
  maxPrice?: number
  colorOptions: string[]
  sizeOptions: string[]
  materialOptions: string[]
  styleOptions: string[]
  minRating?: number
  inStockOnly?: boolean
  hasDiscountOnly?: boolean
  sortBy: string
  sortDirection: string
  page: number
  size: number
}

interface ProductFiltersProps {
  isOpen: boolean
  filters: FilterState
  onFilterChange: (filters: Partial<FilterState>) => void
  categories: Category[]
  brands: Brand[]
  className?: string
}

// Filter options constants
const COLOR_OPTIONS = ['Đen', 'Trắng', 'Be', 'Xanh navy', 'Xanh denim', 'Đỏ', 'Nâu']
const SIZE_OPTIONS = ['S', 'M', 'L', '29', '30', '38', '39', '40', 'Freesize']
const MATERIAL_OPTIONS = ['Cotton', 'Denim', 'Linen', 'Canvas', 'Da']
const STYLE_OPTIONS = ['Basic', 'Oversize', 'Công sở', 'Đường phố']

const PRICE_RANGES = [
  { min: 0, max: 200000, label: 'Dưới 200 nghìn' },
  { min: 200000, max: 400000, label: '200 - 400 nghìn' },
  { min: 400000, max: 700000, label: '400 - 700 nghìn' },
  { min: 700000, max: undefined, label: 'Trên 700 nghìn' }
]

const formatSliderPrice = (price: number): string => {
  if (price >= 1000000) {
    return `${(price / 1000000).toFixed(0)} triệu`
  }
  return `${(price / 1000).toFixed(0)}K`
}

export function ProductFilters({ 
  isOpen, 
  filters, 
  onFilterChange, 
  categories = [], 
  brands = [], 
  className 
}: ProductFiltersProps) {
  const [priceRange, setPriceRange] = useState<[number, number]>([
    filters.minPrice || 0,
    filters.maxPrice || 100000000
  ])

  // Handle checkbox filters
  const handleCheckboxFilter = (
    filterKey: keyof FilterState,
    value: string | number,
    checked: boolean
  ) => {
    const currentValues = (filters[filterKey] as any[]) || []
    let newValues: any[]

    if (checked) {
      newValues = [...currentValues, value]
    } else {
      newValues = currentValues.filter(v => v !== value)
    }

    onFilterChange({ [filterKey]: newValues })
  }

  // Handle price range
  const handlePriceRangeChange = (values: number[]) => {
    setPriceRange([values[0], values[1]])
  }

  const handlePriceRangeCommit = (values: number[]) => {
    onFilterChange({
      minPrice: values[0] > 0 ? values[0] : undefined,
      maxPrice: values[1] < 100000000 ? values[1] : undefined
    })
  }

  // Quick price range selection
  const handleQuickPriceRange = (min: number | undefined, max: number | undefined) => {
    onFilterChange({ minPrice: min, maxPrice: max })
    setPriceRange([min || 0, max || 100000000])
  }

  // Clear filter section
  const clearFilterSection = (section: string) => {
    switch (section) {
      case 'categories':
        onFilterChange({ categoryIds: [] })
        break
      case 'brands':
        onFilterChange({ brandIds: [] })
        break
      case 'price':
        onFilterChange({ minPrice: undefined, maxPrice: undefined })
        setPriceRange([0, 100000000])
        break
      case 'specs':
        onFilterChange({ 
          colorOptions: [], 
          sizeOptions: [], 
          materialOptions: [], 
          styleOptions: [] 
        })
        break
      case 'status':
        onFilterChange({ 
          inStockOnly: false, 
          hasDiscountOnly: false, 
          minRating: undefined 
        })
        break
    }
  }

  // Count active filters
  const getActiveFilterCount = (section: string): number => {
    switch (section) {
      case 'categories':
        return filters.categoryIds.length
      case 'brands':
        return filters.brandIds.length
      case 'price':
        return (filters.minPrice || filters.maxPrice) ? 1 : 0
      case 'specs':
        return filters.colorOptions.length + filters.sizeOptions.length + 
               filters.materialOptions.length + filters.styleOptions.length
      case 'status':
        return (filters.inStockOnly ? 1 : 0) + (filters.hasDiscountOnly ? 1 : 0) + 
               (filters.minRating ? 1 : 0)
      default:
        return 0
    }
  }

  if (!isOpen) return null

  return (
    <Card className={cn("h-fit sticky top-24", className)}>
      <CardHeader className="pb-4">
        <CardTitle className="text-lg flex items-center gap-2">
          <Shirt className="h-5 w-5" />
          Bộ lọc sản phẩm
        </CardTitle>
      </CardHeader>

      <CardContent className="p-0">
        <div className="h-[calc(100vh-200px)] overflow-y-auto">
          <div className="px-6 pb-6 space-y-4">
            
            {/* Categories Filter */}
            <Collapsible defaultOpen>
              <CollapsibleTrigger className="flex items-center justify-between w-full p-2 hover:bg-gray-50 rounded-md">
                <div className="flex items-center justify-between w-full mr-2">
                  <span className="font-medium">Danh mục</span>
                  <div className="flex items-center gap-2">
                    {getActiveFilterCount('categories') > 0 && (
                      <Badge variant="secondary" className="text-xs">
                        {getActiveFilterCount('categories')}
                      </Badge>
                    )}
                    {filters.categoryIds.length > 0 && (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={(e) => {
                          e.stopPropagation()
                          clearFilterSection('categories')
                        }}
                        className="h-4 w-4 p-0 hover:bg-red-100"
                      >
                        <X className="h-3 w-3" />
                      </Button>
                    )}
                  </div>
                </div>
              </CollapsibleTrigger>
              <CollapsibleContent className="p-2">
                <div className="space-y-2">
                  {categories.map((category) => (
                    <div key={category.id} className="flex items-center space-x-2">
                      <Checkbox
                        id={`category-${category.id}`}
                        checked={filters.categoryIds.includes(category.id)}
                        onCheckedChange={(checked) =>
                          handleCheckboxFilter('categoryIds', category.id, !!checked)
                        }
                      />
                      <label
                        htmlFor={`category-${category.id}`}
                        className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer"
                      >
                        {category.name}
                      </label>
                    </div>
                  ))}
                </div>
              </CollapsibleContent>
            </Collapsible>

            {/* Brands Filter */}
            <Collapsible>
              <CollapsibleTrigger className="flex items-center justify-between w-full p-2 hover:bg-gray-50 rounded-md">
                <div className="flex items-center justify-between w-full mr-2">
                  <span className="font-medium">Thương hiệu</span>
                  <div className="flex items-center gap-2">
                    {getActiveFilterCount('brands') > 0 && (
                      <Badge variant="secondary" className="text-xs">
                        {getActiveFilterCount('brands')}
                      </Badge>
                    )}
                    {filters.brandIds.length > 0 && (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={(e) => {
                          e.stopPropagation()
                          clearFilterSection('brands')
                        }}
                        className="h-4 w-4 p-0 hover:bg-red-100"
                      >
                        <X className="h-3 w-3" />
                      </Button>
                    )}
                  </div>
                </div>
              </CollapsibleTrigger>
              <CollapsibleContent className="p-2">
                <div className="space-y-2">
                  {brands.map((brand) => (
                    <div key={brand.id} className="flex items-center space-x-2">
                      <Checkbox
                        id={`brand-${brand.id}`}
                        checked={filters.brandIds.includes(brand.id)}
                        onCheckedChange={(checked) =>
                          handleCheckboxFilter('brandIds', brand.id, !!checked)
                        }
                      />
                      <label
                        htmlFor={`brand-${brand.id}`}
                        className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer"
                      >
                        {brand.name}
                      </label>
                    </div>
                  ))}
                </div>
              </CollapsibleContent>
            </Collapsible>

            {/* Price Range Filter */}
            <Collapsible defaultOpen>
              <CollapsibleTrigger className="flex items-center justify-between w-full p-2 hover:bg-gray-50 rounded-md">
                <div className="flex items-center justify-between w-full mr-2">
                  <span className="font-medium">Khoảng giá</span>
                  <div className="flex items-center gap-2">
                    {getActiveFilterCount('price') > 0 && (
                      <Badge variant="secondary" className="text-xs">
                        {filters.minPrice || filters.maxPrice ? 
                          `${filters.minPrice ? formatSliderPrice(filters.minPrice) : '0'} - ${filters.maxPrice ? formatSliderPrice(filters.maxPrice) : '∞'}`
                          : '1'
                        }
                      </Badge>
                    )}
                    {(filters.minPrice || filters.maxPrice) && (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={(e) => {
                          e.stopPropagation()
                          clearFilterSection('price')
                        }}
                        className="h-4 w-4 p-0 hover:bg-red-100"
                      >
                        <X className="h-3 w-3" />
                      </Button>
                    )}
                  </div>
                </div>
              </CollapsibleTrigger>
              <CollapsibleContent className="p-2">
                <div className="space-y-4">
                  {/* Quick selection buttons */}
                  <div className="grid grid-cols-2 gap-2">
                    {PRICE_RANGES.map((range, index) => (
                      <Button
                        key={index}
                        variant={
                          filters.minPrice === range.min && filters.maxPrice === range.max
                            ? "default"
                            : "outline"
                        }
                        size="sm"
                        onClick={() => handleQuickPriceRange(range.min, range.max)}
                        className="text-xs h-8"
                      >
                        {range.label}
                      </Button>
                    ))}
                  </div>
                  
                  <Separator />
                  
                  {/* Custom range slider */}
                  <div className="space-y-2">
                    <div className="flex justify-between text-sm text-gray-600">
                      <span>{formatSliderPrice(priceRange[0])}</span>
                      <span>{formatSliderPrice(priceRange[1])}</span>
                    </div>
                    <Slider
                      value={priceRange}
                      onValueChange={handlePriceRangeChange}
                      onValueCommit={handlePriceRangeCommit}
                      min={0}
                      max={100000000}
                      step={1000000}
                      className="w-full"
                    />
                  </div>
                </div>
              </CollapsibleContent>
            </Collapsible>

            {/* Technical Specs Filter */}
            <Collapsible defaultOpen>
              <CollapsibleTrigger className="flex items-center justify-between w-full p-2 hover:bg-gray-50 rounded-md">
                <div className="flex items-center justify-between w-full mr-2">
                  <span className="font-medium">Thuộc tính</span>
                  <div className="flex items-center gap-2">
                    {getActiveFilterCount('specs') > 0 && (
                      <Badge variant="secondary" className="text-xs">
                        {getActiveFilterCount('specs')}
                      </Badge>
                    )}
                    {getActiveFilterCount('specs') > 0 && (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={(e) => {
                          e.stopPropagation()
                          clearFilterSection('specs')
                        }}
                        className="h-4 w-4 p-0 hover:bg-red-100"
                      >
                        <X className="h-3 w-3" />
                      </Button>
                    )}
                  </div>
                </div>
              </CollapsibleTrigger>
              <CollapsibleContent className="p-2">
                  <div className="space-y-4">
                    <div>
                      <div className="flex items-center gap-2 mb-2">
                        <span className="text-sm font-medium">Màu sắc</span>
                      </div>
                      <div className="flex flex-wrap gap-2">
                        {COLOR_OPTIONS.map((color) => (
                          <Button
                            key={color}
                            variant={filters.colorOptions.includes(color) ? "default" : "outline"}
                            size="sm"
                            onClick={() => 
                              handleCheckboxFilter('colorOptions', color, !filters.colorOptions.includes(color))
                            }
                            className="h-8 text-xs"
                          >
                            {color}
                          </Button>
                        ))}
                      </div>
                    </div>

                    <div>
                      <div className="flex items-center gap-2 mb-2">
                        <span className="text-sm font-medium">Kích thước</span>
                      </div>
                      <div className="flex flex-wrap gap-2">
                        {SIZE_OPTIONS.map((size) => (
                          <Button
                            key={size}
                            variant={filters.sizeOptions.includes(size) ? "default" : "outline"}
                            size="sm"
                            onClick={() => 
                              handleCheckboxFilter('sizeOptions', size, !filters.sizeOptions.includes(size))
                            }
                            className="h-8 text-xs"
                          >
                            {size}
                          </Button>
                        ))}
                      </div>
                    </div>

                    <div>
                      <div className="flex items-center gap-2 mb-2">
                        <span className="text-sm font-medium">Chất liệu</span>
                      </div>
                      <div className="flex flex-wrap gap-2">
                        {MATERIAL_OPTIONS.map((material) => (
                          <Button
                            key={material}
                            variant={filters.materialOptions.includes(material) ? "default" : "outline"}
                            size="sm"
                            onClick={() => 
                              handleCheckboxFilter('materialOptions', material, !filters.materialOptions.includes(material))
                            }
                            className="h-8 text-xs"
                          >
                            {material}
                          </Button>
                        ))}
                      </div>
                    </div>

                    <div>
                      <div className="flex items-center gap-2 mb-2">
                        <span className="text-sm font-medium">Phong cách</span>
                      </div>
                      <div className="flex flex-wrap gap-2">
                        {STYLE_OPTIONS.map((style) => (
                          <Button
                            key={style}
                            variant={filters.styleOptions.includes(style) ? "default" : "outline"}
                            size="sm"
                            onClick={() => 
                              handleCheckboxFilter('styleOptions', style, !filters.styleOptions.includes(style))
                            }
                            className="h-8 text-xs"
                          >
                            {style}
                          </Button>
                        ))}
                      </div>
                    </div>
                  </div>
                </CollapsibleContent>
              </Collapsible>

              {/* Status & Rating Filter */}
              <Collapsible>
                <CollapsibleTrigger className="flex items-center justify-between w-full p-2 hover:bg-gray-50 rounded-md">
                  <div className="flex items-center justify-between w-full mr-2">
                    <span className="font-medium">Trạng thái & Đánh giá</span>
                    <div className="flex items-center gap-2">
                      {getActiveFilterCount('status') > 0 && (
                        <Badge variant="secondary" className="text-xs">
                          {getActiveFilterCount('status')}
                        </Badge>
                      )}
                      {getActiveFilterCount('status') > 0 && (
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={(e) => {
                            e.stopPropagation()
                            clearFilterSection('status')
                          }}
                          className="h-4 w-4 p-0 hover:bg-red-100"
                        >
                          <X className="h-3 w-3" />
                        </Button>
                      )}
                    </div>
                  </div>
                </CollapsibleTrigger>
                <CollapsibleContent className="p-2">
                  <div className="space-y-4">
                    {/* Stock Status */}
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="inStockOnly"
                        checked={!!filters.inStockOnly}
                        onCheckedChange={(checked) =>
                          onFilterChange({ inStockOnly: !!checked })
                        }
                      />
                      <label
                        htmlFor="inStockOnly"
                        className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer"
                      >
                        Chỉ hiện sản phẩm còn hàng
                      </label>
                    </div>

                    {/* Discount Status */}
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="hasDiscountOnly"
                        checked={!!filters.hasDiscountOnly}
                        onCheckedChange={(checked) =>
                          onFilterChange({ hasDiscountOnly: !!checked })
                        }
                      />
                      <label
                        htmlFor="hasDiscountOnly"
                        className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer"
                      >
                        Chỉ hiện sản phẩm giảm giá
                      </label>
                    </div>

                    {/* Rating Filter */}
                    <div>
                      <span className="text-sm font-medium mb-2 block">
                        Đánh giá tối thiểu
                      </span>
                      <div className="flex flex-wrap gap-2">
                        {[3, 3.5, 4, 4.5, 5].map((rating) => (
                          <Button
                            key={rating}
                            variant={filters.minRating === rating ? "default" : "outline"}
                            size="sm"
                            onClick={() => 
                              onFilterChange({ 
                                minRating: filters.minRating === rating ? undefined : rating 
                              })
                            }
                            className="h-8 text-xs flex items-center gap-1"
                          >
                            <Star className="h-3 w-3" />
                            {rating}+
                          </Button>
                        ))}
                      </div>
                    </div>
                  </div>
                </CollapsibleContent>
              </Collapsible>

          </div>
        </div>
      </CardContent>
    </Card>
  )
}