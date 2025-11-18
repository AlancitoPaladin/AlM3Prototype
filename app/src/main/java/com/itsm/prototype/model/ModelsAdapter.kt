package com.itsm.prototype.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.itsm.prototype.databinding.ItemModelBinding
import java.util.function.Consumer

class ModelsAdapter(
    private val onModelClick: Consumer<Model>
) : ListAdapter<Model, ModelsAdapter.ModelViewHolder>(MODEL_DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        val binding = ItemModelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ModelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        val model = getItem(position)
        holder.bind(model, onModelClick)
    }

    class ModelViewHolder(
        private val binding: ItemModelBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(model: Model, onModelClick: Consumer<Model>) {
            binding.model = model
            binding.onModelClick = onModelClick
            binding.executePendingBindings()
        }
    }

    companion object {
        private val MODEL_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Model>() {
            override fun areItemsTheSame(oldItem: Model, newItem: Model): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Model, newItem: Model): Boolean {
                return oldItem == newItem
            }
        }
    }
}