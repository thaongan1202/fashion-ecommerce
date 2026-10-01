import { useEffect, useState } from 'react'
import type { CSSProperties } from 'react'
import { Link } from 'react-router-dom'
import { getCategories, getPriceRange, getProducts } from '../api/products'
import type { CategoryOption, PageResult, PriceRange, Product } from '../types/product'

export function ProductListPage({ featured = false }: { featured?: boolean }) {
  const [result, setResult] = useState<PageResult<Product> | null>(null)
  const [query, setQuery] = useState('')
  const [categories, setCategories] = useState<CategoryOption[]>([])
  const [priceBounds, setPriceBounds] = useState<PriceRange>({ min: 0, max: 0 })
  const [categoryId, setCategoryId] = useState('')
  const [minPrice, setMinPrice] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const [sort, setSort] = useState('createdAt,desc')
  const [page, setPage] = useState(0)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!featured) Promise.all([getCategories(), getPriceRange()])
      .then(([availableCategories, bounds]) => {
        setCategories(availableCategories)
        setPriceBounds(bounds)
        setMinPrice(String(bounds.min))
        setMaxPrice(String(bounds.max))
      })
      .catch((reason: Error) => setError(reason.message))
  }, [featured])

  useEffect(() => {
    const params = new URLSearchParams({ page: String(page), size: featured ? '4' : '12', sort: featured ? 'createdAt,desc' : sort })
    if (query.trim()) params.set('q', query.trim())
    if (!featured && categoryId) params.set('categoryId', categoryId)
    if (!featured && minPrice) params.set('minPrice', minPrice)
    if (!featured && maxPrice) params.set('maxPrice', maxPrice)
    getProducts(params).then(setResult).catch((reason: Error) => setError(reason.message))
  }, [page, query, categoryId, minPrice, maxPrice, sort, featured])

  const priceSpan = priceBounds.max - priceBounds.min
  const minPosition = priceSpan > 0 ? ((Number(minPrice) - priceBounds.min) / priceSpan) * 100 : 0
  const maxPosition = priceSpan > 0 ? ((Number(maxPrice) - priceBounds.min) / priceSpan) * 100 : 100
  const formatPrice = (value: number) => `${value.toLocaleString('vi-VN')} ₫`

  return (
    <section className={featured ? 'catalog-section featured-products' : 'catalog-section page-shell'}>
      <div className="catalog-heading">
        <div><p className="eyebrow">BỘ SƯU TẬP</p><h2>{featured ? 'Lựa chọn nổi bật' : 'Sản phẩm'}</h2></div>
        {!featured && <label className="search-field">Tìm kiếm<input value={query} onChange={(event) => { setQuery(event.target.value); setPage(0) }} placeholder="Tên sản phẩm..." /></label>}
      </div>
      {!featured && <div className="catalog-filters">
        <label>Danh mục<select value={categoryId} onChange={(event) => { setCategoryId(event.target.value); setPage(0) }}><option value="">Tất cả danh mục</option>{categories.map((category) => <option value={category.id} key={category.id}>{category.name}</option>)}</select></label>
        <div className="price-range-filter">
          <div className="price-range-labels"><span>Khoảng giá</span><strong>{formatPrice(Number(minPrice || priceBounds.min))} – {formatPrice(Number(maxPrice || priceBounds.max))}</strong></div>
          <div className="price-range-control" style={{ '--range-left': `${minPosition}%`, '--range-right': `${100 - maxPosition}%` } as CSSProperties}>
            <input aria-label="Giá thấp nhất" type="range" min={priceBounds.min} max={priceBounds.max} step="1000" value={Number(minPrice || priceBounds.min)} disabled={priceSpan <= 0} onChange={(event) => { setMinPrice(String(Math.min(Number(event.target.value), Number(maxPrice || priceBounds.max)))); setPage(0) }} />
            <input aria-label="Giá cao nhất" type="range" min={priceBounds.min} max={priceBounds.max} step="1000" value={Number(maxPrice || priceBounds.max)} disabled={priceSpan <= 0} onChange={(event) => { setMaxPrice(String(Math.max(Number(event.target.value), Number(minPrice || priceBounds.min)))); setPage(0) }} />
          </div>
        </div>
        <label>Sắp xếp<select value={sort} onChange={(event) => { setSort(event.target.value); setPage(0) }}><option value="createdAt,desc">Mới nhất</option><option value="price,asc">Giá thấp đến cao</option><option value="price,desc">Giá cao đến thấp</option></select></label>
      </div>}
      {error && <p className="notice notice-error" role="alert">{error}</p>}
      {!result && !error && <p>Đang tải sản phẩm...</p>}
      {result?.content.length === 0 && <p>Chưa tìm thấy sản phẩm phù hợp.</p>}
      <div className="product-grid">
        {result?.content.map((product) => {
          const image = product.images.find((item) => item.primary)?.imageUrl ?? product.images[0]?.imageUrl
          return <Link className="product-card" to={`/products/${product.id}`} key={product.id}>
            <div className="product-image">{image ? <img src={image} alt={product.name} /> : <span>FASHION / STORE</span>}</div>
            <div className="product-info"><p>{product.brandName}</p><h3>{product.name}</h3><span>{product.minPrice === product.maxPrice ? product.minPrice.toLocaleString('vi-VN') : `${product.minPrice.toLocaleString('vi-VN')} – ${product.maxPrice.toLocaleString('vi-VN')}`} ₫</span><span className="product-rating">{'★'.repeat(Math.round(product.averageRating))}{'☆'.repeat(5 - Math.round(product.averageRating))} {product.averageRating.toFixed(1)} · {product.reviewCount} đánh giá</span></div>
          </Link>
        })}
      </div>
      {!featured && result && result.totalPages > 1 && <nav className="pagination" aria-label="Phân trang">
        <button disabled={page === 0} onClick={() => setPage((value) => value - 1)}>Trước</button><span>Trang {page + 1} / {result.totalPages}</span><button disabled={page + 1 >= result.totalPages} onClick={() => setPage((value) => value + 1)}>Tiếp</button>
      </nav>}
    </section>
  )
}
