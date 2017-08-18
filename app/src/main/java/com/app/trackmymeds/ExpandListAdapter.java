package com.app.trackmymeds;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Declan on 23/06/2017.
 */

public class ExpandListAdapter extends BaseExpandableListAdapter
{

	public ExpandListAdapter(Context _context, ArrayList<ExpandListGroup> _groups)
	{
		m_context = _context;
		m_groups = _groups;
	}

	public void addItem(ExpandListChild item, ExpandListGroup group)
	{
		if (!m_groups.contains(group))
		{
			m_groups.add(group);
		}

		int index = m_groups.indexOf(group);
		ArrayList<ExpandListChild> child = m_groups.get(index).getItems();
		child.add(item);
		m_groups.get(index).setItems(child);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition)
	{
		ArrayList<ExpandListChild> childList = m_groups.get(groupPosition).getItems();
		return childList.get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition)
	{
		return childPosition;
	}

	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent)
	{
		ExpandListChild child = (ExpandListChild) getChild(groupPosition, childPosition);
		if (view == null)
		{
			LayoutInflater infalInflater = (LayoutInflater) m_context.getSystemService(m_context.LAYOUT_INFLATER_SERVICE);
			view = infalInflater.inflate(R.layout.expandlist_child_item, null);
		}

		TextView tv = (TextView) view.findViewById(R.id.tvChild);
		tv.setText(child.getName().toString());
		tv.setTag(child.getTag());

		return view;
	}

	public int getChildrenCount(int groupPosition)
	{
		ArrayList<ExpandListChild> childList = m_groups.get(groupPosition).getItems();

		if (childList != null)
		{
			return childList.size();
		}

		return 0;
	}

	public Object getGroup(int groupPosition)
	{
		return m_groups.get(groupPosition);
	}

	public int getGroupCount()
	{
		return m_groups.size();
	}

	public long getGroupId(int groupPosition)
	{
		return groupPosition;
	}

	public View getGroupView(int groupPosition, boolean isLastChild, View view, ViewGroup parent)
	{
		ExpandListGroup group = (ExpandListGroup) getGroup(groupPosition);
		if (view == null)
		{
			LayoutInflater inf = (LayoutInflater) m_context.getSystemService(m_context.LAYOUT_INFLATER_SERVICE);
			view = inf.inflate(R.layout.expandlist_group_item, null);
		}

		TextView tv = (TextView) view.findViewById(R.id.tvGroup);
		tv.setText(group.getName());

		return view;
	}

	public boolean hasStableIds()
	{
		return true;
	}

	public boolean isChildSelectable(int arg0, int arg1)
	{
		return true;
	}

	//Properties.
	private Context m_context;
	private ArrayList<ExpandListGroup> m_groups;
}
