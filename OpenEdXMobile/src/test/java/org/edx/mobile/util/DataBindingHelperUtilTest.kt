package org.edx.mobile.util

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when` as mockitoWhen

class DataBindingHelperUtilTest {

    @Test
    fun testIsViewVisible() {
        val mockedImageView = Mockito.mock(ImageView::class.java)
        DataBindingHelperUtils.isViewVisible(mockedImageView, false)
        mockitoWhen(mockedImageView.visibility).thenReturn(View.INVISIBLE)
        assertThat(mockedImageView.visibility).isEqualTo(View.INVISIBLE)
    }

    @Test
    fun testSetText() {
        val mockedTextView = Mockito.mock(TextView::class.java)
        DataBindingHelperUtils.setText(mockedTextView, "Test")
        mockitoWhen(mockedTextView.text).thenReturn("Test")
        assertThat(mockedTextView.text.toString()).isEqualTo("Test")
    }
}
