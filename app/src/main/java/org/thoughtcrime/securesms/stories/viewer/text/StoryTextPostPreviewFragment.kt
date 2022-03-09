package org.thoughtcrime.securesms.stories.viewer.text

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import org.signal.core.util.DimensionUnit
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.linkpreview.LinkPreview
import org.thoughtcrime.securesms.mediapreview.MediaPreviewFragment
import org.thoughtcrime.securesms.stories.StoryTextPostView
import org.thoughtcrime.securesms.stories.viewer.page.StoryPost
import org.thoughtcrime.securesms.util.CommunicationActions
import org.thoughtcrime.securesms.util.Projection
import org.thoughtcrime.securesms.util.fragments.requireListener
import kotlin.math.roundToInt

class StoryTextPostPreviewFragment : Fragment(R.layout.stories_text_post_preview_fragment) {

  companion object {
    private const val STORY_ID = "STORY_ID"

    fun create(content: StoryPost.Content.TextContent): Fragment {
      return StoryTextPostPreviewFragment().apply {
        arguments = Bundle().apply {
          putParcelable(MediaPreviewFragment.DATA_URI, content.uri)
          putLong(STORY_ID, content.recordId)
        }
      }
    }
  }

  private val viewModel: StoryTextPostViewModel by viewModels(
    factoryProducer = {
      StoryTextPostViewModel.Factory(requireArguments().getLong(STORY_ID), StoryTextPostRepository())
    }
  )

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val storyTextPostView: StoryTextPostView = view.findViewById(R.id.story_text_post)

    viewModel.state.observe(viewLifecycleOwner) { state ->
      when (state.loadState) {
        StoryTextPostState.LoadState.INIT -> Unit
        StoryTextPostState.LoadState.LOADED -> {
          storyTextPostView.bindFromStoryTextPost(state.storyTextPost!!)
          storyTextPostView.bindLinkPreview(state.linkPreview)

          if (state.linkPreview != null) {
            storyTextPostView.setLinkPreviewClickListener {
              showLinkPreviewTooltip(it, state.linkPreview)
            }
          } else {
            storyTextPostView.setLinkPreviewClickListener(null)
          }
        }
        StoryTextPostState.LoadState.FAILED -> {
          requireListener<MediaPreviewFragment.Events>().mediaNotAvailable()
        }
      }
    }
  }

  @SuppressLint("AlertDialogBuilderUsage")
  private fun showLinkPreviewTooltip(view: View, linkPreview: LinkPreview) {
    requireListener<Callback>().setIsDisplayingLinkPreviewTooltip(true)

    val contentView = LayoutInflater.from(requireContext()).inflate(R.layout.stories_link_popup, null, false)

    contentView.findViewById<TextView>(R.id.url).text = linkPreview.url
    contentView.setOnClickListener {
      CommunicationActions.openBrowserLink(requireContext(), linkPreview.url)
    }

    contentView.measure(
      View.MeasureSpec.makeMeasureSpec(DimensionUnit.DP.toPixels(275f).toInt(), View.MeasureSpec.EXACTLY),
      0
    )

    contentView.layout(0, 0, contentView.measuredWidth, contentView.measuredHeight)

    val alertDialog = AlertDialog.Builder(requireContext())
      .setView(contentView)
      .create()

    alertDialog.window!!.attributes = alertDialog.window!!.attributes.apply {
      val rootProjection = Projection.relativeToViewRoot(view.rootView, null)
      val viewProjection = Projection.relativeToViewRoot(view, null).translateY(view.translationY)

      val dialogBottom = rootProjection.height / 2f + contentView.measuredHeight / 2f
      val linkPreviewViewTop = viewProjection.y

      rootProjection.release()
      viewProjection.release()

      val delta = linkPreviewViewTop - dialogBottom
      this.y = delta.roundToInt()
    }
    alertDialog.window!!.setDimAmount(0f)
    alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    alertDialog.setOnDismissListener {
      requireListener<Callback>().setIsDisplayingLinkPreviewTooltip(false)
    }
    alertDialog.show()
  }

  interface Callback {
    fun setIsDisplayingLinkPreviewTooltip(isDisplayingLinkPreviewTooltip: Boolean)
  }
}
