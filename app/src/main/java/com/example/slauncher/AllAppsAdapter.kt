package com.example.slauncher

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView

class AllAppsAdapter(
    private val context: Context,
    private val allApps: List<AppInfo>
) : BaseAdapter(), Filterable {
    
    private var filteredApps: List<AppInfo> = allApps
    
    override fun getCount(): Int = filteredApps.size
    
    override fun getItem(position: Int): Any = filteredApps[position]
    
    override fun getItemId(position: Int): Long = position.toLong()
    
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.app_list_item, parent, false)
        
        val app = filteredApps[position]
        val appName = view.findViewById<TextView>(R.id.app_name)
        
        appName.text = app.appName
        
        return view
    }
    
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim()
                
                val filtered = if (query.isNullOrEmpty()) {
                    allApps
                } else {
                    allApps.filter { app ->
                        app.appName.lowercase().contains(query)
                    }
                }
                
                return FilterResults().apply {
                    values = filtered
                    count = filtered.size
                }
            }
            
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                @Suppress("UNCHECKED_CAST")
                filteredApps = results?.values as? List<AppInfo> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }
}