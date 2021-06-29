package com.example.whatsappclone.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.R
import com.example.whatsappclone.viewholders.UserViewHolder
import com.example.whatsappclone.activities.ChatActivity
import com.example.whatsappclone.activities.IMAGE
import com.example.whatsappclone.activities.NAME
import com.example.whatsappclone.activities.UID
import com.example.whatsappclone.models.UserModel
import com.example.whatsappclone.viewholders.EmptyViewHolder
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_people.*

private const val DELETED_VIEW_TYPE = 1
private const val NORMAL_VIEW_TYPE = 2

class PeopleFragment : Fragment() {

    private lateinit var mAdapter: FirestorePagingAdapter<UserModel, RecyclerView.ViewHolder>
    val auth by lazy {
        FirebaseAuth.getInstance()
    }
    private val database by lazy {
        FirebaseFirestore.getInstance().collection("users")
            .orderBy("name", Query.Direction.DESCENDING)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setUpAdapter()
        return inflater.inflate(R.layout.fragment_people, container, false)
    }

    private fun setUpAdapter() {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false).setPrefetchDistance(2).setPageSize(10).build()

        val options = FirestorePagingOptions.Builder<UserModel>()
            .setLifecycleOwner(viewLifecycleOwner).setQuery(database, config, UserModel::class.java).build()

        mAdapter = object :FirestorePagingAdapter<UserModel, RecyclerView.ViewHolder>(options){

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

                return when(viewType){
                    NORMAL_VIEW_TYPE -> UserViewHolder(
                        layoutInflater.inflate(R.layout.list_item, parent, false)
                    )
                    else -> EmptyViewHolder(
                        layoutInflater.inflate(R.layout.empty_view, parent, false)
                    )
                }
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: UserModel) {
                if(holder is UserViewHolder){
                    holder.bind(userModel = model){ name: String, photo: String, id: String ->
                        val intent = Intent(requireContext(), ChatActivity::class.java)
                        intent.putExtra(UID, id)
                        intent.putExtra(NAME, name)
                        intent.putExtra(IMAGE, photo)
                        startActivity(intent)
                    }
                }
            }

            override fun getItemViewType(position: Int): Int {
                val item = getItem(position)?.toObject(UserModel::class.java)
                return if(auth.uid == item?.uid){
                    DELETED_VIEW_TYPE
                } else{
                    NORMAL_VIEW_TYPE
                }
            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        peopleRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
    }
}
