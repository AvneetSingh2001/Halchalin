package com.avicodes.halchalin.presentation.ui.home.explore

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.avicodes.halchalin.data.models.News
import com.avicodes.halchalin.data.utils.Result
import com.avicodes.halchalin.databinding.FragmentNewsVpBinding
import com.avicodes.halchalin.presentation.ui.home.HomeActivity
import com.avicodes.halchalin.presentation.ui.home.HomeActivityViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class NewsVpFragment : Fragment() {

    private var _binding : FragmentNewsVpBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: NewsViewPagerAdapter
    private lateinit var viewModel: HomeActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentNewsVpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as HomeActivity).viewModel
        setUpLocalNewsRecyclerView()
        getNewsList()
        binding.videoViewPager.setPageTransformer(DepthPageTransformer())
        observeExploreTab()

        adapter.setOnCommentClickListener {
            showCommentDialog(it)
        }

        adapter.setOnShareClickListener {
            viewModel.createDeepLink(it)
        }

        adapter.setOnSeeMoreClickListener {
            showNewsDescDialog(it)
        }

        observeLinkCreated()
    }

    private fun observeLinkCreated() {
        viewModel.linkCreated.observe(viewLifecycleOwner, Observer {
            when(it) {
                is Result.Loading -> {
                }
                is Result.Success -> {
                    it.data?.let {link ->
                        shareLink(link)
                    }
                }
                is Result.Error-> {
                    Toast.makeText(requireContext(), "Error sharing news", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        })
    }
    private fun shareLink(link: String?) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "$link")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share News")
        startActivity(shareIntent)
    }

    private fun showNewsDescDialog(desc: String) {
        val action = NewsVpFragmentDirections.actionNewsVpFragmentToDescFragment(desc)
        requireView().findNavController().navigate(action)
    }

    private fun showCommentDialog(news: News) {
        val action = NewsVpFragmentDirections.actionNewsVpFragmentToCommentFragment(news)
        requireView().findNavController().navigate(action)
    }

    private fun observeExploreTab() {
        viewModel.exploreNewsTab.observe(requireActivity(), Observer {
            when(it) {
                is Result.Success -> {
                    it.data?.let { it1 -> binding.videoViewPager.currentItem = it1 }
                    viewModel.exploreNewsTab.value = Result.NotInitialized
                }
                else -> {}
            }
        })
    }

    private fun getNewsList() {
        viewModel.localHeadlines.observe(viewLifecycleOwner, Observer {response ->
            when(response) {
                is Result.Error -> {
                    hideProgressBar()
                    Toast.makeText(context,"An Error Occurred", Toast.LENGTH_LONG).show()
                    Log.e("Error", response.exception?.message.toString())
                }

                is Result.Success -> {
                    hideProgressBar()
                    response.data?.let {
                        adapter.differ.submitList(it)
                    }
                }

                else -> {
                    showProgressBar()
                }
            }
        })
    }

    private fun showProgressBar() {
        binding.mainCons.visibility = View.INVISIBLE
    }


    private fun hideProgressBar() {
        binding.mainCons.visibility = View.VISIBLE
    }

    private fun setUpLocalNewsRecyclerView() {
        binding.apply {
            adapter = NewsViewPagerAdapter()
            videoViewPager.adapter = adapter
        }
    }

    class DepthPageTransformer : ViewPager2.PageTransformer {

        private val MIN_SCALE= 0.75F

        override fun transformPage(view: View, position: Float) {
            view.apply {

                val pageWidth = width
                when {
                    position < -1 -> { // [-Infinity,-1)
                        // This page is way off-screen to the left.
                        alpha = 0f
                    }
                    position <= 0 -> { // [-1,0]
                        // Use the default slide transition when moving to the left page
                        alpha = 1f
                        translationY = 0f
                        translationZ = 0f
                        scaleY = 1f
                        scaleX = 1f
                    }
                    position <= 1 -> { // (0,1]
                        // Fade the page out.
                        alpha = 1 - position

                        // Counteract the default slide transition
                        translationY = pageWidth * -position
                        // Move it behind the left page
                        translationZ = -1f

                        // Scale the page down (between MIN_SCALE and 1)
                        val scaleFactor = (MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position)))
                        scaleY = scaleFactor
                        scaleX = scaleFactor
                    }
                    else -> { // (1,+Infinity]
                        // This page is way off-screen to the right.
                        alpha = 0f
                    }
                }
            }
        }
    }
}

