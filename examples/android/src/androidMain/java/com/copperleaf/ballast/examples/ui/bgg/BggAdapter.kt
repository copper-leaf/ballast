package com.copperleaf.ballast.examples.ui.bgg

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.Disposable
import com.copperleaf.ballast.examples.api.models.BggHotListItem
import com.copperleaf.ballast.examples.databinding.ListItemBggBinding

class BggAdapter(
    private val items: List<BggHotListItem>
) : RecyclerView.Adapter<BggAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemBggBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindPost(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(
        private val binding: ListItemBggBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        private var imageLoaderDisposable: Disposable? = null
        fun bindPost(item: BggHotListItem) {
            imageLoaderDisposable?.dispose()

            binding.tvPublishedDate.text = "Published ${item.yearPublished}"
            binding.tvTitle.text = item.name

            imageLoaderDisposable = binding.ivBoxArt.load(item.thumbnail)
        }
    }
}
