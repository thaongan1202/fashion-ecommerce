'use client';

/**
 * ProductForm - Form to create new products with dynamic metadata fields
 */

import { useState, useEffect, useMemo } from 'react';
import { productAPI, adminAPI } from '@/lib/api';
import type { CreateProductRequest, ProductMetadata } from '@/types';
import type { CategoryResponse } from '@/types/category';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { toast } from 'sonner';
import { getMetadataFields, groupMetadataFields, type MetadataField } from '@/lib/constants/categoryMetadata';

interface ProductFormProps {
  onSuccess?: () => void;
}

export function ProductForm({ onSuccess }: ProductFormProps) {
  const [loading, setLoading] = useState(false);
  const [categories, setCategories] = useState<Array<{ id: number; name: string }>>([]);
  const [brands, setBrands] = useState<Array<{ id: number; name: string }>>([]);
  const [metadata, setMetadata] = useState<ProductMetadata>({});
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});
  const [formData, setFormData] = useState<CreateProductRequest>({
    name: '',
    description: '',
    thumbnailUrl: '',
    categoryId: 1,
    brandId: 1,
    status: true,
    templates: [
      {
        sku: '',
        color: '',
        size: '',
        price: 0,
        stockQuantity: 0,
        status: true,
      },
    ],
    metadata: {},
    images: [],
  });

  // Get selected category name
  const selectedCategory = useMemo(() => {
    return categories.find(cat => cat.id === formData.categoryId);
  }, [categories, formData.categoryId]);

  // Get metadata fields for selected category
  const metadataFields = useMemo(() => {
    if (!selectedCategory) return [];
    return getMetadataFields(selectedCategory.name);
  }, [selectedCategory]);

  // Group metadata fields
  const groupedFields = useMemo(() => {
    return groupMetadataFields(metadataFields);
  }, [metadataFields]);

  // Constants
  const PRICE_ROUNDING_FACTOR = 1000;
  const EXCLUDED_CATEGORY = 'Phụ kiện';

  // Fill random data for testing
  const fillRandomData = () => {
    const randomNum = (min: number, max: number, decimals = 0) => {
      const value = Math.random() * (max - min) + min;
      return decimals > 0 ? parseFloat(value.toFixed(decimals)) : Math.floor(value);
    };

    const randomChoice = <T,>(arr: T[]): T => arr[randomNum(0, arr.length)];

    const colors = ['Đen', 'Trắng', 'Xanh navy', 'Be', 'Xám'];
    const sizes = ['S', 'M', 'L', 'XL'];
    const names = ['Áo thun basic cotton', 'Áo sơ mi oversize', 'Quần jeans ống rộng', 'Sneaker trắng basic', 'Túi tote canvas'];

    if (!selectedCategory) return;

    const productName = randomChoice(names);
    const price = Math.floor(randomNum(199000, 1290000, 0) / PRICE_ROUNDING_FACTOR) * PRICE_ROUNDING_FACTOR;
    const description = 'Sản phẩm thời trang dễ phối, chất liệu thoáng và phù hợp mặc hàng ngày.';

    // Fill basic info
    setFormData({
      ...formData,
      name: productName,
      description: description,
      thumbnailUrl: 'https://via.placeholder.com/400x400.png?text=Product',
      templates: [{
        sku: `SKU${Date.now()}`,
        color: randomChoice(colors),
        size: randomChoice(sizes),
        price: price,
        stockQuantity: randomNum(10, 100),
        status: true,
      }],
    });

    const newMetadata: Record<string, string> = {
      material: randomChoice(['Cotton', 'Denim', 'Kaki', 'Linen', 'Canvas']),
      style: randomChoice(['Basic', 'Công sở', 'Đường phố', 'Tối giản']),
      targetAudience: randomChoice(['Nam', 'Nữ', 'Unisex']),
      season: 'Quanh năm',
      pattern: 'Trơn',
      fit: randomChoice(['Regular', 'Oversize', 'Ống rộng']),
      origin: 'Việt Nam',
      careInstructions: 'Giặt máy ở nhiệt độ thấp, không tẩy, phơi nơi thoáng mát.',
    };

    setMetadata(newMetadata);
    toast.success('Đã điền dữ liệu random thành công!', {
      description: 'Kiểm tra lại thông tin trước khi lưu',
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Validation
    if (!formData.name || formData.name.length < 5) {
      toast.error('Tên sản phẩm phải có ít nhất 5 ký tự');
      return;
    }

    if (!formData.templates[0].sku || !formData.templates[0].price || formData.templates[0].stockQuantity === undefined) {
      toast.error('Vui lòng điền đầy đủ SKU, Giá và Tồn kho cho biến thể sản phẩm');
      return;
    }

    try {
      setLoading(true);
      
      // Filter out empty/invalid metadata fields
      const cleanedMetadata: any = {};
      Object.entries(metadata).forEach(([key, value]) => {
        if (value !== '' && value !== null && value !== undefined) {
          cleanedMetadata[key] = value;
        }
      });
      
      // Merge metadata into formData
      const submitData = {
        ...formData,
        metadata: Object.keys(cleanedMetadata).length > 0 ? cleanedMetadata : undefined,
      };
      
      const response = await productAPI.create(submitData);
      
      // Show success message
      toast.success('Tạo sản phẩm thành công!', {
        description: `Sản phẩm "${formData.name}" đã được thêm vào danh sách.`,
        duration: 5000,
      });
      
      // Reset form
      setFormData({
        name: '',
        description: '',
        thumbnailUrl: '',
        categoryId: categories[0]?.id || 1,
        brandId: brands[0]?.id || 1,
        status: true,
        templates: [
          {
            sku: '',
            color: '',
            size: '',
            price: 0,
            stockQuantity: 0,
            status: true,
          },
        ],
        metadata: {},
        images: [],
      });
      setMetadata({});
      setValidationErrors({});
      
      onSuccess?.();
    } catch (err) {
      // Try to extract validation errors from response
      let errorMessage = 'Không thể tạo sản phẩm';
      let errorDescription = '';
      const validationErrs: Record<string, string> = {};
      
      if (err instanceof Error) {
        errorMessage = err.message;
        
        // Try to parse validation errors from error object
        // Backend sends errors in data.data object
        try {
          const errObj = err as any;
          if (errObj.data && typeof errObj.data === 'object') {
            Object.entries(errObj.data).forEach(([key, msg]) => {
              validationErrs[key] = msg as string;
            });
            setValidationErrors(validationErrs);
            
            // Build error list for description
            const errorList = Object.entries(validationErrs)
              .map(([field, msg]) => `• ${field}: ${msg}`)
              .join('\n');
            
            if (errorList) {
              errorMessage = 'Lỗi validation';
              errorDescription = errorList;
            }
          }
        } catch (parseErr) {
          console.error('Failed to parse validation errors:', parseErr);
        }
        
        // If it's a generic validation error, add helpful hints
        if (errorMessage.includes('Validation failed') && Object.keys(validationErrs).length === 0) {
          errorDescription = 'Vui lòng kiểm tra tên sản phẩm, SKU, giá, tồn kho và thông tin thời trang.';
        }
        
        // Network or server errors
        if (errorMessage.includes('Network') || errorMessage.includes('fetch')) {
          errorMessage = 'Lỗi kết nối';
          errorDescription = 'Không thể kết nối tới server. Vui lòng kiểm tra kết nối mạng.';
        }
      }
      
      // Show error toast
      toast.error(errorMessage, {
        description: errorDescription,
        duration: 7000,
      });
    } finally {
      setLoading(false);
    }
  };

  // Load categories and brands on mount
  useEffect(() => {
    const loadData = async () => {
      try {
        const [catsRes, brandsRes] = await Promise.all([
          adminAPI.getAllCategories(),
          adminAPI.getAllBrands()
        ]);
        if (catsRes.success && catsRes.data) {
          // Filter out excluded category
          const filteredCategories = catsRes.data.filter((c: CategoryResponse) => c.name !== EXCLUDED_CATEGORY);
          setCategories(filteredCategories);
        }
        if (brandsRes.success && brandsRes.data) setBrands(brandsRes.data);
      } catch (err) {
        console.error('Failed to load categories/brands:', err);
      }
    };
    loadData();
  }, []);

  const updateTemplate = (index: number, field: string, value: any) => {
    const newTemplates = [...formData.templates];
    newTemplates[index] = { ...newTemplates[index], [field]: value };
    setFormData({ ...formData, templates: newTemplates });
  };

  const updateMetadata = (fieldName: string, value: any) => {
    setMetadata(prev => ({ ...prev, [fieldName]: value }));
  };

  const renderMetadataField = (field: MetadataField) => {
    const value = metadata[field.name] ?? '';

    switch (field.type) {
      case 'number':
        return (
          <Input
            type="number"
            value={value}
            onChange={(e) => updateMetadata(field.name, Number(e.target.value))}
            placeholder={field.placeholder}
            required={field.required}
            min={field.min}
            max={field.max}
            step="1"
          />
        );
      
      case 'select':
        return (
          <select
            value={value}
            onChange={(e) => updateMetadata(field.name, e.target.value)}
            className="w-full px-3 py-2 border border-input rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
            required={field.required}
          >
            <option value="">-- Chọn --</option>
            {field.options?.map(opt => (
              <option key={opt} value={opt}>{opt}</option>
            ))}
          </select>
        );
      
      case 'textarea':
        return (
          <textarea
            value={value}
            onChange={(e) => updateMetadata(field.name, e.target.value)}
            placeholder={field.placeholder}
            className="w-full px-3 py-2 border border-input rounded-md min-h-[80px]"
            required={field.required}
          />
        );
      
      case 'boolean':
        return (
          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              checked={!!value}
              onChange={(e) => updateMetadata(field.name, e.target.checked)}
              className="w-4 h-4"
            />
            <span className="text-sm text-muted-foreground">Có</span>
          </div>
        );
      
      default: // text
        return (
          <Input
            type="text"
            value={value}
            onChange={(e) => updateMetadata(field.name, e.target.value)}
            placeholder={field.placeholder}
            required={field.required}
          />
        );
    }
  };

  return (
    <form onSubmit={handleSubmit} className="bg-card rounded-lg border shadow-sm p-6 space-y-6">
      {/* Quick Fill Button */}
      <div className="flex justify-between items-center pb-4 border-b">
        <h2 className="text-xl font-bold">Thêm sản phẩm mới</h2>
        <button
          type="button"
          onClick={fillRandomData}
          className="px-4 py-2 bg-gradient-to-r from-purple-500 to-pink-500 text-white rounded-md hover:from-purple-600 hover:to-pink-600 transition-all shadow-md flex items-center gap-2"
        >
          <span>🎲</span>
          <span>Điền Random (Test)</span>
        </button>
      </div>

      {/* Thông tin cơ bản */}
      <div className="space-y-4">
        <h3 className="font-semibold text-lg">Thông tin cơ bản</h3>
        
        <div className="space-y-2">
          <Label htmlFor="name">Tên sản phẩm *</Label>
          <Input
            id="name"
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            placeholder="VD: Áo thun basic cotton"
            required
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="description">Mô tả</Label>
          <textarea
            id="description"
            value={formData.description}
            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            placeholder="Mô tả sản phẩm..."
            className="w-full px-3 py-2 border rounded-md min-h-[100px]"
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="thumbnailUrl">URL ảnh đại diện</Label>
          <Input
            id="thumbnailUrl"
            value={formData.thumbnailUrl}
            onChange={(e) => setFormData({ ...formData, thumbnailUrl: e.target.value })}
            placeholder="https://example.com/image.jpg"
          />
          <p className="text-xs text-muted-foreground">
            Để trống nếu chưa có. Có thể cập nhật sau.
          </p>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="categoryId">Danh mục *</Label>
            <select
              id="categoryId"
              value={formData.categoryId}
              onChange={(e) => setFormData({ ...formData, categoryId: Number(e.target.value) })}
              className="w-full px-3 py-2 border border-input rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
              required
            >
              {categories.map(cat => (
                <option key={cat.id} value={cat.id}>{cat.name}</option>
              ))}
            </select>
          </div>
          <div className="space-y-2">
            <Label htmlFor="brandId">Thương hiệu *</Label>
            <select
              id="brandId"
              value={formData.brandId}
              onChange={(e) => setFormData({ ...formData, brandId: Number(e.target.value) })}
              className="w-full px-3 py-2 border border-input rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
              required
            >
              {brands.map(brand => (
                <option key={brand.id} value={brand.id}>{brand.name}</option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Thông số kỹ thuật (Metadata) - Dynamic based on category */}
      {metadataFields.length > 0 && (
        <div className="space-y-4">
          <h3 className="font-semibold text-lg">Thông số kỹ thuật ({selectedCategory?.name})</h3>
          <p className="text-sm text-muted-foreground">
            Các thông số này sẽ giúp khách hàng hiểu rõ hơn về sản phẩm
          </p>

          {Object.entries(groupedFields).map(([groupName, fields]) => (
            <div key={groupName} className="space-y-3 p-4 border rounded-lg bg-muted/20">
              <h4 className="font-medium text-sm text-primary">{groupName}</h4>
              <div className="grid grid-cols-2 gap-4">
                {fields.map(field => {
                  const errorKey = `metadata.${field.name}`;
                  const hasError = !!validationErrors[errorKey];
                  
                  return (
                    <div key={field.name} className="space-y-2">
                      <Label htmlFor={field.name} className={hasError ? 'text-destructive' : ''}>
                        {field.label} {field.required && <span className="text-destructive">*</span>}
                      </Label>
                      {renderMetadataField(field)}
                      {hasError && (
                        <p className="text-sm text-destructive font-medium">
                          {validationErrors[errorKey]}
                        </p>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Template (Variant) */}
      <div className="space-y-4">
        <h3 className="font-semibold text-lg">Biến thể sản phẩm (Template) *</h3>
        <p className="text-sm text-muted-foreground">Phải có ít nhất 1 biến thể</p>

        <div className="grid grid-cols-2 gap-4 p-4 border rounded-lg">
          <div className="space-y-2">
            <Label>SKU * (VD: AT-DEN-M)</Label>
            <Input
              value={formData.templates[0].sku}
              onChange={(e) => updateTemplate(0, 'sku', e.target.value)}
              placeholder="Mã SKU unique"
              required
            />
          </div>
          <div className="space-y-2">
            <Label>Màu sắc</Label>
            <Input
              value={formData.templates[0].color}
              onChange={(e) => updateTemplate(0, 'color', e.target.value)}
              placeholder="VD: Đen"
            />
          </div>
          <div className="space-y-2">
            <Label>Kích thước</Label>
            <Input
              value={formData.templates[0].size}
              onChange={(e) => updateTemplate(0, 'size', e.target.value)}
              placeholder="VD: M"
            />
          </div>
          <div className="space-y-2">
            <Label>Giá *</Label>
            <Input
              type="number"
              value={formData.templates[0].price}
              onChange={(e) => updateTemplate(0, 'price', Number(e.target.value))}
              placeholder="27990000"
              required
            />
          </div>
          <div className="space-y-2">
            <Label>Tồn kho *</Label>
            <Input
              type="number"
              value={formData.templates[0].stockQuantity}
              onChange={(e) => updateTemplate(0, 'stockQuantity', Number(e.target.value))}
              placeholder="50"
              required
            />
          </div>
        </div>
      </div>

      {/* Actions */}
      <div className="flex items-center gap-3 pt-4">
        <Button type="submit" disabled={loading}>
          {loading ? 'Đang tạo...' : 'Tạo sản phẩm'}
        </Button>
        <Button
          type="button"
          variant="outline"
          onClick={() => {
            setFormData({
              name: '',
              description: '',
              thumbnailUrl: '',
              categoryId: categories[0]?.id || 1,
              brandId: brands[0]?.id || 1,
              status: true,
              templates: [
                {
                  sku: '',
                  color: '',
                  size: '',
                  price: 0,
                  stockQuantity: 0,
                  status: true,
                },
              ],
              metadata: {},
              images: [],
            });
            setMetadata({});
          }}
        >
          Reset
        </Button>
      </div>
    </form>
  );
}
