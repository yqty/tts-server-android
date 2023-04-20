package com.github.jing332.tts_server_android.ui.forwarder.system

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.databinding.SysTtsForwarderFragmentBinding
import com.github.jing332.tts_server_android.help.config.SysTtsForwarderConfig
import com.github.jing332.tts_server_android.service.forwarder.system.SysTtsForwarderService
import com.github.jing332.tts_server_android.utils.MyTools
import com.github.jing332.tts_server_android.utils.reduceDragSensitivity
import com.github.jing332.tts_server_android.utils.toast

class SysTtsForwarderHostFragment : Fragment(R.layout.sys_tts_forwarder_fragment) {
    private val binding by viewBinding(SysTtsForwarderFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.forwarder_system, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_open_web -> {
                        if (SysTtsForwarderService.instance?.isRunning == true) {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data =
                                Uri.parse("http://localhost:${SysTtsForwarderConfig.port}")
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        } else {
                            toast(R.string.server_please_start_service)
                        }
                        true
                    }
                    R.id.menu_wake_lock -> {
                        menuItem.isChecked = !menuItem.isChecked
                        SysTtsForwarderConfig.isWakeLockEnabled = menuItem.isChecked
                        toast(R.string.server_restart_service_to_update)
                        true
                    }
                    R.id.menu_shortcut -> {
                        MyTools.addShortcut(
                            requireContext(),
                            getString(R.string.forwarder_systts),
                            "forwarder_system",
                            R.mipmap.ic_launcher_round,
                            Intent(requireContext(), ScSwitchActivity::class.java)
                        )
                        true
                    }
                    else -> false
                }
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                menu.findItem(R.id.menu_wake_lock)?.isChecked =
                    SysTtsForwarderConfig.isWakeLockEnabled
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.vp.reduceDragSensitivity(8)
//        binding.vp.isSaveEnabled = false
        binding.vp.adapter = FragmentAdapter(this)
        binding.vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.bnv.menu.getItem(position).isChecked = true
            }
        })
        binding.bnv.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_serverLog -> binding.vp.setCurrentItem(0, true)
                R.id.menu_serverWeb -> binding.vp.setCurrentItem(1, true)
            }
            true
        }
    }

    inner class FragmentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        private val logPage by lazy { SysTtsForwarderLogPage() }
        private val webPage by lazy { SysTtsForwarderWebPage() }

        private val fragmentList = listOf(logPage, webPage)
        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }
    }


}