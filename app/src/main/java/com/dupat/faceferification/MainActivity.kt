package com.dupat.faceferification

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dupat.demofaceverificationsecurity.DetectorActivity
import com.dupat.faceferification.databinding.ActivityMainBinding
import com.dupat.faceferification.db.SecurityDatabase
import com.dupat.faceferification.repositories.SecurityDatabaseRepository
import com.dupat.faceferification.utils.Function
import com.dupat.faceferification.utils.Function.byteArrayToBitmap
import com.dupat.faceferification.utils.snackbar
import com.dupat.faceferification.viewmodel.DataSetSecurityViewModel
import com.dupat.faceferification.viewmodel.factory.DataSetSecurityViewModelFactory
import com.dupat.faceferification.viewmodel.state.ViewState
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(),View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: DataSetSecurityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val dao = SecurityDatabase.getInstance(application).dataSetSecurityDao
        val repository = SecurityDatabaseRepository(dao)
        val factory = DataSetSecurityViewModelFactory(repository)
        viewModel = ViewModelProvider(this,factory).get(DataSetSecurityViewModel::class.java)
        binding.viewmodel = viewModel

        btnChooseImage.setOnClickListener(this)
        btnLoadImage.setOnClickListener(this)
        handleViewState()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnChooseImage -> {
                if(etUsername.text.toString().isNullOrEmpty()){
                    containerMain.snackbar("Enter your name first!")
                }
                else{
                    val intent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )

                    intent.type = "image/*"
                    startActivityForResult(intent, 10)
                }
            }

            R.id.btnLoadImage -> {
                showSavedImage()
            }
        }
    }

    private fun showSavedImage() {
        viewModel.getDatSet().observe(this, androidx.lifecycle.Observer {
//                toast(th.size.toString())
            val data = it[0]
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.image_edit_dialog, null)
            val ivFace =
                dialogLayout.findViewById<ImageView>(R.id.dlg_image)
            val tvTitle = dialogLayout.findViewById<TextView>(R.id.dlg_title)
            val etName = dialogLayout.findViewById<EditText>(R.id.dlg_input)
            tvTitle.text = "Saved Image"
            ivFace.setImageBitmap(byteArrayToBitmap(data.imageData))
            etName.setText(data.imageName)
            builder.setPositiveButton("OK", DialogInterface.OnClickListener { dlg, i ->
                val name = etName.text.toString()
                if (name.isEmpty()) {
                    return@OnClickListener
                }
                //knownFaces.put(name, rec);
                dlg.dismiss()
            })
            builder.setView(dialogLayout)
            builder.show()

        })
    }

    private fun getRealpath(uri: Uri): String {
        var realPath: String? = null
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = contentResolver.query(uri, filePathColumn, null, null, null)
        if (cursor!!.moveToFirst()) {
            val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
            realPath = cursor.getString(columnIndex)
        }
        cursor.close()
        return realPath!!
    }

    fun handleViewState(){
        viewModel.getState().observer(this, Observer {
            when(it){
                is ViewState.IsLoading -> {
                    containerMain.snackbar("Loading...")
                }

                is ViewState.Error -> {
                    containerMain.snackbar("Error: ${it.err!!}")
                }

                is ViewState.IsSuccess -> {
                    when(it.what){
                        0 -> {
                            viewModel.securityName = etUsername.text.toString()
                            viewModel.imageUrl = "https://google.com"
                            viewModel.insertDataSet()
                        }

                        1 -> {
//                            containerMain.snackbar("Success insert data")
                            startActivity(Intent(this,DetectorActivity::class.java))
                        }

                        2 -> {

                        }
                    }
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            // When an Image is picked
            if (requestCode == 10 && resultCode == RESULT_OK) {
                if (data?.data != null) {
                    val mImageUri: Uri = data.data!!
                    val realPath: String = getRealpath(mImageUri)
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, mImageUri)
                    ivPerson.setImageBitmap(bitmap)
                    viewModel.bmpImage = bitmap
                    viewModel.validateImage()
                    Log.d(
                        "TAG",
                        "onActivityResult: " + realPath + " uri: " + mImageUri + " string: " + mImageUri.toString()
                    )
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Something went wrong: ${e.message}", Toast.LENGTH_LONG)
                .show()
        }
    }
}