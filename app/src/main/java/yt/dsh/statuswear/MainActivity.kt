package yt.dsh.statuswear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import yt.dsh.statuswear.ui.StatusScreen
import yt.dsh.statuswear.ui.StatusViewModel
import yt.dsh.statuswear.ui.theme.DshStatusTheme

class MainActivity : ComponentActivity() {

    private val viewModel: StatusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DshStatusTheme {
                StatusScreen(viewModel = viewModel)
            }
        }
    }
}
