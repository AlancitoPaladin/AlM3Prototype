package com.itsm.prototype.model

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.itsm.prototype.R
import com.itsm.prototype.databinding.ItemModelBinding
import com.itsm.prototype.ui.seller.ModelItem
import java.util.function.Consumer

class ModelsAdapter(
    private val onModelClick: (ModelItem) -> Unit
) : ListAdapter<ModelItem, ModelsAdapter.ModelViewHolder>(ModelDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        val binding = ItemModelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ModelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ModelViewHolder(
        private val binding: ItemModelBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(model: ModelItem) {
            binding.apply {
                tvModelName.text = model.name
                tvModelDescription.text = model.description
                tvModelPrice.text = "$${model.price}"
                "Tipo: ${model.detectedObject}".also { tvDetectedObject.text = it }

                Glide.with(itemView.context)
                    .load(model.modelUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_error)
                    .into(ivModelThumbnail)

                root.setOnClickListener {
                    onModelClick(model)
                }
            }
        }
    }

    class ModelDiffCallback : DiffUtil.ItemCallback<ModelItem>() {
        override fun areItemsTheSame(oldItem: ModelItem, newItem: ModelItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ModelItem, newItem: ModelItem): Boolean {
            return oldItem == newItem
        }
    }
}