package nl.stekkinger.nizi.fragments.doctor


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import nl.stekkinger.nizi.R

/**
 * A simple [Fragment] subclass.
 */

// Not used
class PatientDiaryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }


}
