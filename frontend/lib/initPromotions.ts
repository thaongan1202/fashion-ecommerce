// Mã giảm giá mặc định, khớp dữ liệu seed ở backend
import type { Promotion } from '@/types/api-cart';

const initPromotions: Promotion[] = [
  {
    id: 'promo-sale10',
    code: 'SALE10',
    title: 'Giảm 10%',
    description: 'Giảm 10% cho mọi đơn, tối đa 200.000đ',
    percent_discount: 10,
    max_discount: 200000,
    min_value_to_be_applied: 0,
    status: 'ACTIVE',
    template_type: 'VOUCHER',
    templateCode: 'VOUCHER_TEMPLATE',
  },
  {
    id: 'promo-giam50k',
    code: 'GIAM50K',
    title: 'Giảm 50.000đ',
    description: 'Giảm 50.000đ cho đơn từ 200.000đ',
    fixed_amount: 50000,
    min_value_to_be_applied: 200000,
    status: 'ACTIVE',
    template_type: 'VOUCHER',
    templateCode: 'VOUCHER_TEMPLATE',
  },
  {
    id: 'promo-freeship',
    code: 'FREESHIP',
    title: 'Miễn phí vận chuyển',
    description: 'Miễn phí vận chuyển cho mọi đơn',
    min_value_to_be_applied: 0,
    status: 'ACTIVE',
    template_type: 'FREESHIP',
    templateCode: 'FREESHIP_TEMPLATE',
  },
];

export default initPromotions;
