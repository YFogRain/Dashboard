package com.dash.mylibrary.utils

import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope

val appLifecycleScope by lazy { ProcessLifecycleOwner.get().lifecycleScope }