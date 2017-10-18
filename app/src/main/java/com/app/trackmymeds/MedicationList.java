package com.app.trackmymeds;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Declan on 23/06/2017.
 */

public class MedicationList extends BaseAdapter
{
	public MedicationList(Context _context, ArrayList<MedicationBrand> _items)
	{
		m_context = _context;
		m_items = _items;
	}

	public void addItem(MedicationBrand item)
	{
		if (!m_items.contains(item))
		{
			m_items.add(item);
		}
	}

	public void addAll(ArrayList<MedicationBrand> items)
	{
		m_items.addAll(items);
	}

	public void clear()
	{
		m_items.clear();
	}

	@Override
	public int getCount()
	{
		return m_items.size();
	}

	@Override
	public Object getItem(int itemPosition)
	{
		return m_items.get(itemPosition);
	}

	@Override
	public long getItemId(int position)
	{
		return m_items.get(position).hashCode();
	}

	@Override
	public View getView(int itemPosition, View view, ViewGroup parent)
	{
		MedicationBrand brand = (MedicationBrand)getItem(itemPosition);
		if (view == null)
		{
			LayoutInflater inflater = (LayoutInflater)m_context.getSystemService(m_context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.medication_list_item, null);
		}

		TextView tv = (TextView)view.findViewById(R.id.tvItem);
		tv.setText(brand.toString());
		//tv.setTag(child.getTag());

		return view;
	}

	//Properties.
	private Context m_context;
	private ArrayList<MedicationBrand> m_items;
}