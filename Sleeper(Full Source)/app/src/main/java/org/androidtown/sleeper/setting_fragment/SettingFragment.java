package org.androidtown.sleeper.setting_fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.androidtown.sleeper.R;

/**
 * Created by Administrator on 2015-08-31.
 */
@Deprecated
public class SettingFragment extends Fragment implements AdapterView.OnItemClickListener {

    private View rootView=null ;
    private ListView settingList=null ;
    private ArrayAdapter<String> arrayAdapter=null ;

    private String[] optionsString={OPTION_SUPPORTED_PROCESSOR_SELECT} ;

    private static final String OPTION_SUPPORTED_PROCESSOR_SELECT="Processor Select" ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView=inflater.inflate(R.layout.layout_setting,container,false) ;

        settingList=(ListView)rootView.findViewById(R.id.listViewSettingList) ;

        arrayAdapter=new clSettingListAdapter(getActivity(),R.layout.layout_setting_item,R.id.textViewProcessorSelect) ;

        //add item
        arrayAdapter.add(OPTION_SUPPORTED_PROCESSOR_SELECT) ;

        settingList.setAdapter(arrayAdapter) ;

        settingList.setOnItemClickListener(this);


        return rootView ;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        clSettingListAdapter arrayAdapter=(clSettingListAdapter)parent.getAdapter() ;

        String myItemId=arrayAdapter.getId(position) ;

        if(myItemId.equals(OPTION_SUPPORTED_PROCESSOR_SELECT)) {

            getActivity().getSupportFragmentManager().beginTransaction().
                    replace(R.id.fragmentContainer, new ProcessorSelectFragment()).addToBackStack(null).commit();
        }

        Toast.makeText(getActivity(),"Item Clicked!!",Toast.LENGTH_SHORT).show() ;
    }

    private class clSettingListAdapter extends ArrayAdapter<String>{

        public clSettingListAdapter(Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
        }

        public String getId(int position){

            return optionsString[position] ;
        }
    }
}
