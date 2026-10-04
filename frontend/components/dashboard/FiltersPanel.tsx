"use client"

import React from "react"
import { useDashboardStore } from "../../store/dashboardStore"

export default function FiltersPanel() {
  const { filters, setFilters } = useDashboardStore()

  return (
    <div className="space-y-3">
      <div>
        <label className="text-xs text-slate-500">Từ ngày</label>
        <input className="w-full mt-1 p-2 rounded border" type="date" value={filters.start} onChange={(e)=>setFilters({...filters, start: e.target.value})} />
      </div>
      <div>
        <label className="text-xs text-slate-500">Đến ngày</label>
        <input className="w-full mt-1 p-2 rounded border" type="date" value={filters.end} onChange={(e)=>setFilters({...filters, end: e.target.value})} />
      </div>
      <div>
        <label className="text-xs text-slate-500">Tìm kiếm</label>
        <input className="w-full mt-1 p-2 rounded border" placeholder="Mã đơn / email" value={filters.search} onChange={(e)=>setFilters({...filters, search: e.target.value})} />
      </div>
      <div className="flex gap-2">
        <button className="px-3 py-2 rounded bg-indigo-600 text-white" onClick={()=>setFilters({...filters, quick:'7d'})}>7 ngày</button>
        <button className="px-3 py-2 rounded bg-indigo-100 text-indigo-700" onClick={()=>setFilters({...filters, quick:'30d'})}>30 ngày</button>
      </div>
    </div>
  )
}
