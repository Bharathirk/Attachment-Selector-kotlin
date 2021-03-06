package com.example.admin.mvpinitialprojectsetupkotlin.ui.fragments

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import com.example.admin.mvpinitialprojectsetupkotlin.R
import com.example.admin.mvpinitialprojectsetupkotlin.adapter.*
import com.example.admin.mvpinitialprojectsetupkotlin.app.AppController
import com.example.admin.mvpinitialprojectsetupkotlin.base.BaseFragment
import com.example.admin.mvpinitialprojectsetupkotlin.base.MainThreadBus
import com.example.admin.mvpinitialprojectsetupkotlin.data.eventBus.*
import com.example.admin.mvpinitialprojectsetupkotlin.data.model.FolderItem
import com.example.admin.mvpinitialprojectsetupkotlin.data.model.HeaderItemModel
import com.example.admin.mvpinitialprojectsetupkotlin.data.model.ImageDataModel
import com.example.admin.mvpinitialprojectsetupkotlin.utils.DateTimeUtils
import com.example.admin.mvpinitialprojectsetupkotlin.utils.GalleryHelper
import com.squareup.otto.Subscribe
import com.truizlop.sectionedrecyclerview.SectionedSpanSizeLookup
import io.reactivex.Flowable
import io.reactivex.functions.Function
import kotlinx.android.synthetic.main.fragment_images.*
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ImagesFragment : BaseFragment(), FolderAdapter.ClickManager, RadioGroup.OnCheckedChangeListener, ImageSectionGridAdapter.onGridImageClickedListner, ImagesListAdapter.onFolderImageClickedListner {

    override fun onGridSectionClicked(imageModel: List<ImageDataModel>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    private var recAdapter: ImagesListAdapter? = null
    private var folderAdapter: FolderAdapter? = null
    private var allImageList: List<ImageDataModel>? = null
    private var allIFolderItem: List<FolderItem>? = null
    private var isFolderDetailEnable: Boolean? = false
    private var bus: MainThreadBus? = null
    private var sectionAdapter: ImageSectionGridAdapter? = null
    private var headerList = ArrayList<HeaderItemModel>()
    private var stringListMap = HashMap<String, List<ImageDataModel>>()
    private var layoutManager: GridLayoutManager? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_images, container, false);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bus = AppController.getInstanse()!!.getBus()
        bus!!.register(this)

        folderAdapter = FolderAdapter(activity!!, this)
        sectionAdapter = ImageSectionGridAdapter(activity!!, this)

        rg_image_type.setOnCheckedChangeListener(this)
        rg_image_type.check(R.id.rb_all_image)
        bus!!.post(RequestDataEvent(true))

    }

    @Subscribe
    fun reciveAllImages(event: ImagesDataEvent) {
        allImageList = event.imgList
        convertToMap()
    }

    @Subscribe
    fun reciveAllFolders(event: FolderDataEvent) {
        allIFolderItem = event.folderList
    }

    private fun convertToMap() {

        Flowable.just<List<ImageDataModel>>(allImageList).flatMapIterable { list -> list }
                .groupBy { imageDataModel -> imageDataModel.getDate() }
                .flatMapSingle { dateImageDataModelGroupedFlowable ->
                    dateImageDataModelGroupedFlowable.toList()
                }.toMap(Function<List<ImageDataModel>, String> { list ->
                    val modelItem = list[0]
                    modelItem.dateCreated!!
                }).subscribe { stringListMap, throwable ->
                    setToAdapter(stringListMap);
                    Log.e("ddd", stringListMap.size.toString() + "")
                }
    }

    private fun setToAdapter(stringListMap: Map<String, List<ImageDataModel>>) {

        this.stringListMap.clear()
        headerList.clear()
        stringListMap.keys.forEach { headerList.add(HeaderItemModel(it, DateTimeUtils.convertToDate(it)!!, false)) }

        Collections.sort(headerList, object : Comparator<HeaderItemModel> {
            override fun compare(lhs: HeaderItemModel?, rhs: HeaderItemModel?): Int {
                return rhs!!.date!!.compareTo(lhs!!.date)

            }

        })
        this.stringListMap.putAll(stringListMap)
        setSectionRecDatas()

    }

    private fun setSectionRecDatas() {
        layoutManager = GridLayoutManager(activity, 3)
        val lookup = SectionedSpanSizeLookup(sectionAdapter, layoutManager)
        layoutManager!!.spanSizeLookup = lookup
        images_rec_view.adapter = sectionAdapter
        images_rec_view.layoutManager = layoutManager

        sectionAdapter!!.setImageDatas(stringListMap, headerList)
    }


    override fun onCheckedChanged(group: RadioGroup?, p1: Int) {
        when (group!!.checkedRadioButtonId) {
            R.id.rb_all_image -> {
                setSectionRecDatas()
                isFolderDetailEnable = false
            }
            R.id.rb_by_folder -> setFolderImage()
        }
    }

    override fun onGridImageClicked(imageModel: ImageDataModel) {
        bus!!.post(imageModel)
    }

    override fun onFolderImageClicked(imageModel: ImageDataModel) {
        bus!!.post(imageModel)
        allImageList!!.forEach {
            if (it.imgId.equals(imageModel.imgId))
                it.selected = imageModel.selected
        }
    }


    private fun setFolderImage() {
        layoutManager = GridLayoutManager(activity, 3)
        images_rec_view.layoutManager = layoutManager

        isFolderDetailEnable = false
        images_rec_view.adapter = folderAdapter
        folderAdapter!!.setFolderItemList(allIFolderItem!!)
    }


    private fun setAllImages(itemList: List<ImageDataModel>) {
        recAdapter = ImagesListAdapter(activity!!, false, this)
        images_rec_view.adapter = recAdapter
        recAdapter!!.setImageList(itemList)
    }


    override fun onItemClick(folderItem: FolderItem) {
        isFolderDetailEnable = true
        bus!!.post(RequestFolderDataEvent(true, folderItem.folderId!!))
    }

    @Subscribe
    fun reciveFolderDetailImages(event: FolderDetailImagesDataEvent) {
        setAllImages(event.imgList)
    }

    @Subscribe
    public fun onHomeBackPressed(event: HomeBackPressEvent) {
        if (isFolderDetailEnable!!)
            setFolderImage()
        else activity!!.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bus!!.unregister(this)
    }
}