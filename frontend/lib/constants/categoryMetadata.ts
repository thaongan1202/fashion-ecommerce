/**
 * Metadata thời trang dùng chung cho mọi danh mục.
 */

export interface MetadataField {
  name: string;
  label: string;
  type: 'text' | 'number' | 'select' | 'textarea' | 'boolean';
  options?: string[];
  placeholder?: string;
  required?: boolean;
  group?: string;
  min?: number;
  max?: number;
}

const FASHION_FIELDS: MetadataField[] = [
  { name: 'material', label: 'Chất liệu', type: 'select', group: 'Thông tin sản phẩm', options: ['Cotton', 'Denim', 'Kaki', 'Linen', 'Polyester', 'Canvas', 'Da'] },
  { name: 'style', label: 'Phong cách', type: 'select', group: 'Thông tin sản phẩm', options: ['Basic', 'Công sở', 'Đường phố', 'Thể thao', 'Tối giản'] },
  { name: 'targetAudience', label: 'Đối tượng sử dụng', type: 'select', group: 'Thông tin sản phẩm', options: ['Nam', 'Nữ', 'Unisex'] },
  { name: 'season', label: 'Mùa', type: 'select', group: 'Thông tin sản phẩm', options: ['Xuân Hè', 'Thu Đông', 'Quanh năm'] },
  { name: 'pattern', label: 'Họa tiết', type: 'text', group: 'Thông tin sản phẩm', placeholder: 'Trơn, kẻ, hoa' },
  { name: 'fit', label: 'Kiểu dáng', type: 'text', group: 'Thông tin sản phẩm', placeholder: 'Regular, oversize, ống rộng' },
  { name: 'origin', label: 'Xuất xứ', type: 'text', group: 'Thông tin sản phẩm', placeholder: 'Việt Nam' },
  { name: 'careInstructions', label: 'Hướng dẫn bảo quản', type: 'textarea', group: 'Bảo quản', placeholder: 'Giặt máy ở nhiệt độ thấp, không tẩy' },
  { name: 'additionalSpecs', label: 'Thông tin thêm', type: 'textarea', group: 'Khác' },
];

export const CATEGORY_METADATA: Record<string, MetadataField[]> = {
  'Quần áo': FASHION_FIELDS,
  'Giày dép': FASHION_FIELDS,
  'Túi xách': FASHION_FIELDS,
  'Phụ kiện': FASHION_FIELDS,
};

export function getMetadataFields(_categoryName: string): MetadataField[] {
  return FASHION_FIELDS;
}

export function groupMetadataFields(fields: MetadataField[]): Record<string, MetadataField[]> {
  const grouped: Record<string, MetadataField[]> = {};

  fields.forEach(field => {
    const group = field.group || 'Khác';
    if (!grouped[group]) {
      grouped[group] = [];
    }
    grouped[group].push(field);
  });

  return grouped;
}
