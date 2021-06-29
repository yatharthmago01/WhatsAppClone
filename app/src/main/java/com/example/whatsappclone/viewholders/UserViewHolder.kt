package com.example.whatsappclone.viewholders

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.R
import com.example.whatsappclone.models.UserModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.list_item.view.*

class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

    fun bind(userModel: UserModel, onClick: (name: String, photo: String, id: String) -> Unit) = with(itemView){

        countTv.isVisible = false
        timeTv.isVisible = false

        titleTv.text = userModel.name
        subtitleTv.text = userModel.status

        Picasso.get().load(userModel.thumbImageUrl)
            .placeholder(R.drawable.ic_default_avatar)
            .error(R.drawable.ic_default_avatar).into(userImgView)

        setOnClickListener{
            onClick.invoke(userModel.name, userModel.thumbImageUrl, userModel.uid)
        }
    }
}