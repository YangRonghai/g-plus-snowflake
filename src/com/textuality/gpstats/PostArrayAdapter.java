package com.textuality.gpstats;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PostArrayAdapter extends ArrayAdapter<Post> implements AdapterView.OnItemClickListener {
    
    private final LayoutInflater mInflater;
    private final Context mContext;

    public PostArrayAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Post post = getItem(position);
        LinearLayout layout = (LinearLayout) mInflater.inflate(R.layout.list_item, parent, false);
        TextView plusOnes = (TextView) layout.findViewById(R.id.plusOnes);
        TextView text = (TextView) layout.findViewById(R.id.text);
        plusOnes.setText("+" + post.plusOnes());
        text.setText(post.text());
        return layout;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Post post = getItem(position);
        Intent i = new Intent(Intent.ACTION_VIEW, post.uri());
        mContext.startActivity(i);
    }
}
