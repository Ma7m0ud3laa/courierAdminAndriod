package com.kadabra.agent.records

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.transition.Transition
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.loader.glide.GlideImageLoader
import com.github.piasy.biv.view.BigImageView
import com.kadabra.Utilities.Base.BaseFragment
import com.kadabra.agent.R
import com.kadabra.agent.callback.IBottomSheetCallback
import com.kadabra.agent.utilities.AppConstants
import com.kadabra.agent.utilities.sendShareIntent
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


/**
 * A simple [Fragment] subclass.
 */
class TaskImageFragment : BaseFragment(), IBottomSheetCallback, View.OnClickListener {
    private var TAG = this.javaClass.simpleName
    private lateinit var parent: RelativeLayout
    private var listener: IBottomSheetCallback? = null
    private lateinit var ivShare: ImageView
    private lateinit var ivTaskImage: BigImageView
    private lateinit var ivTaskImage1: ImageView

    private lateinit var ivBack: ImageView
    private lateinit var currentView: View
    private lateinit var bitMap: Bitmap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        BigImageViewer.initialize(GlideImageLoader.with(context!!));
        currentView = inflater.inflate(R.layout.fragment_task_image, container, false)
        parent = currentView.findViewById(R.id.parent)
        ivShare = currentView.findViewById(R.id.ivShare)
        ivTaskImage = currentView.findViewById(R.id.ivTaskImage)
        ivBack = currentView.findViewById(R.id.ivBack)

        ivShare.setOnClickListener(this)
        ivTaskImage.setOnClickListener(this)
        ivBack.setOnClickListener(this)


//        Glide.with(activity!! /* context */)
//            .load(AppConstants.CURRENT_IMAGE_URI)
//            .into(ivTaskImage)

        Log.d(TAG, AppConstants.CURRENT_IMAGE_URI.toString())

        ivTaskImage.showImage(Uri.parse(AppConstants.CURRENT_IMAGE_URI.toString()));

//        Glide.with(this)
//            .asBitmap()
//            .load(AppConstants.CURRENT_IMAGE_URI.toString())
//            .into(object : CustomTarget<Bitmap>() {
//                override fun onResourceReady(
//                    resource: Bitmap,
//                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
//                ) {
//                    Log.d(TAG,"bitMap "+bitMap)
//                    bitMap = resource
//                    saveImageToInternal(bitMap)
//                }
//
//                override fun onLoadCleared(placeholder: Drawable?) {
//                    // this is called when imageView is cleared on lifecycle call or for
//                    // some other reason.
//                    // if you are referencing the bitmap somewhere else too other than this imageView
//                    // clear it here as you can no longer have the bitmap
//                }
//
//
//            })

//        ivTaskImage.setImageURI(AppConstants.CURRENT_IMAGE_URI)

        return currentView
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.ivShare -> {
//                ivTaskImage.invalidate()
//                val drawable = ivTaskImage.background.toBitmap()
//                shareImage()

//                bitMap = getBitmapFromView(parent)!!
                var v1 = activity!!.window.decorView.rootView;
//              bitMap=  screenShot(v1)
                context!!.sendShareIntent(AppConstants.CURRENT_IMAGE_URI.toString())
//                share()
            }
            R.id.ivBack -> {
                listener?.onBottomSheetSelectedItem(6)
            }

        }
    }

    fun getBitmapFromView(view: View): Bitmap? { //Define a bitmap with the same size as the view
        val returnedBitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val bgDrawable = view.background
        if (bgDrawable != null) //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas) else  //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
        return returnedBitmap
    }


    private fun saveImageToInternal(bitmap: Bitmap) {
        try {
            val cachePath = File(context!!.cacheDir, "images")
            cachePath.mkdirs() // don't forget to make the directory
            val stream =
                FileOutputStream("$cachePath/image.png") // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            Log.d(TAG, "saveImageToInternal")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun shareImage() {
        Log.d(TAG, "shareImage")
        val imagePath = File(context!!.cacheDir, "images")
        val newFile = File(imagePath, "image.png")
        val contentUri =
            FileProvider.getUriForFile(context!!, "com.kadabra.agent.fileprovider", newFile)
        Log.d(TAG, contentUri.toString())
        if (contentUri != null) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
            shareIntent.setDataAndType(contentUri, context!!.contentResolver.getType(contentUri))
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            startActivity(Intent.createChooser(shareIntent, "Choose an app"))
        }
    }

    private fun share() {

        var imageUri = Uri.parse(
            MediaStore.Images.Media.insertImage(
                context!!.contentResolver,
                bitMap,
                "title",
                "discription"
            )
        );
        var shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND;
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.type = "image/*";
        startActivity(Intent.createChooser(shareIntent, "Share"));
    }

    override fun onBottomSheetClosed(isClosed: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBottomSheetSelectedItem(index: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IBottomSheetCallback) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    public fun screenShot(view:View ):Bitmap {
    var bitmap = Bitmap.createBitmap(view.getWidth(),
            view.getHeight(), Bitmap.Config.ARGB_8888);
    var canvas =  Canvas(bitmap);
    view.draw(canvas);
        Log.d(TAG,"data"+bitmap)
    return bitmap;
}
}
