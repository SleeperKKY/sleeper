//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : Sleeper
//  @ File Name : clStatManager
//  @ Date : 2015-09-06
//  @ Author : Kang Shin Wook, Kim Hyun Woong, Kim Hyun Woo
//  @ Email : rkdtlsdnr102@naver.com

package org.androidtown.sleeper.propclasses.dataprocessor_manager;

import android.content.Context;

import com.jjoe64.graphview.GraphView;

import java.util.ArrayList;
import java.util.List;

public class clStatManager {
	private List<GraphView> graphList=null;
	private List<String> staticData=null;
	private List<String> staticDataName=null;
	private Context AttachedContext=null ;
	private double[] xData=null ;
	private int dataSize=0 ;

	public clStatManager(Context context) {

		AttachedContext=context ;
		graphList=new ArrayList<>() ;
		staticData=new ArrayList<>() ;
		staticDataName=new ArrayList<>() ;
	}
	
	public void addGraph(GraphView graph) {

		graphList.add(graph) ;

	}
	
	public List getGraphList() {

		return graphList ;
	}

	public void setDataSize(int size){

		dataSize=size ;
	}
	
	public void addStaticData(String dataName, String data) {

		staticDataName.add(dataName) ;
		staticData.add(data) ;
	}
	
	public List getStaticDataList() {

		return staticData ;
	}

	public int getDataSize(){

		return dataSize ;
	}

	public List getStaticDataNameList(){

		return staticDataName ;
	}

	public void setXData(double[] data){

		xData=data ;
	}

	public double[] getXData(){

		return xData ;
	}
}
