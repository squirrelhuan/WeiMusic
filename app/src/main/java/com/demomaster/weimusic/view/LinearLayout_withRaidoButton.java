package com.demomaster.weimusic.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.List;

public class LinearLayout_withRaidoButton extends LinearLayout {

	private RadioButton radioButton;
	private List<RadioButton> list = new ArrayList<RadioButton>();

	public LinearLayout_withRaidoButton(Context context, AttributeSet attrs,
                                        int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.setOnClickListener(null);
		// TODO Auto-generated constructor stub
	}

	public LinearLayout_withRaidoButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOnClickListener(null);
		// TODO Auto-generated constructor stub
	}

	public LinearLayout_withRaidoButton(Context context) {
		super(context);
		this.setOnClickListener(null);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setOnClickListener(final OnClickListener l) {
		OnClickListener l2 = new OnClickListener() {
			@Override
			public void onClick(View v) {
				//OnClickListener ref = l;
				try {
					/*Method method = ref.getClass().getMethod("onClick",
							new Class[] { View.class });
					System.out.println("反射方法名" + method.getName());
					method.invoke(l,
							new Object[] { LinearLayout_withRaidoButton.this });*/
					for (int i = 0; i < list.size(); i++) {
						list.get(i).setChecked(false);
					}
					radioButton.setChecked(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		super.setOnClickListener(l2);
	}

	public RadioButton getRadioButton() {
		return radioButton;
	}

	public void setRadioButton(RadioButton radioButton) {
		this.radioButton = radioButton;
		this.radioButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LinearLayout_withRaidoButton.this.performClick();
			}
		});
		if(!list.contains(this.radioButton)){
			list.add(this.radioButton);
		}
	}

	public List<RadioButton> getList() {
		return list;
	}

	public void setList(List<RadioButton> list) {
		this.list = list;
	}
	public void setDefault(RadioButton radioButton){
		radioButton.setChecked(true);
	}

}
