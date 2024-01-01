package com.assignment.theapp.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.assignment.theapp.databinding.ListItemTaskBinding
import com.assignment.theapp.model.Task

class TaskAdapter : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private lateinit var tasks: List<Task>
    private var filteredTasks: List<Task> = emptyList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemTaskBinding.inflate(inflater, parent, false)
        return TaskViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return filteredTasks.size
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = filteredTasks[position]
        holder.bind(task)
    }

    fun submitList(newTasks: List<Task>) {
        tasks = newTasks
        filter(null)
        notifyDataSetChanged()
    }

    class TaskViewHolder(private val binding: ListItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.task = task
            binding.executePendingBindings()
        }
    }

    fun filter(query: String?) {
        filteredTasks = if (query.isNullOrBlank()) {
            tasks
        } else {
            tasks.filter { task ->
                task.task.contains(query, ignoreCase = true) ||
                        task.title.contains(query, ignoreCase = true) ||
                        task.description.contains(query, ignoreCase = true) ||
                        task.colorCode.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }

}

