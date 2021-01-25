package com.example.peach;

import java.util.HashMap;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<AudioMessage> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<AudioMessage, Pair<String,String>> _listDataChild;
    private Runnable mRunnable;
    private FirebaseUser user = null;

    public ExpandableListAdapter(Context context, List<AudioMessage> listDataHeader,
                                 HashMap<AudioMessage, Pair<String,String>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this.user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition));
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        Pair<String,String> childText = (Pair<String,String>) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.child_item, null);
        }

        TextView timeCreated = convertView
                .findViewById(R.id.time_text);

        EditText tagText = convertView
                .findViewById(R.id.tag_text);

        tagText.setText(childText.first);
        timeCreated.setText(childText.second);

        tagText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                changeTagText(groupPosition, s.toString(),childText.second);
                //notifyDataSetChanged();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        return convertView;
    }

    public void changeTagText(int groupPosition, String s,String tm) {
        Pair<String,String> newPair = new Pair<>(s, tm);
        this._listDataChild.put((AudioMessage) getGroup(groupPosition),newPair);
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getGroupView(int i, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        MessageViewHolderExt holder = new MessageViewHolderExt();
        LayoutInflater messageInflater = (LayoutInflater) _context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        AudioMessage message = _listDataHeader.get(i);

        final Handler mHandler = new Handler();

        if (message.getUserID().equals(user.getUid())) { // this message was sent by us so let's create a basic chat bubble on the right
            convertView = messageInflater.inflate(R.layout.new_my_message, parent, false);
            convertView.setTag(holder);

            holder.seekBody = convertView.findViewById(R.id.seek_bar);
            holder.messageBody = message.getMedia();
            holder.mButtonPlay = convertView.findViewById(R.id.btn_play);
            holder.timePass = convertView.findViewById(R.id.timeView);
            holder.timePass.setText("0:00");
            holder.rl = convertView.findViewById(R.id.my_relative);


        } else { // this message was sent by someone else so let's create an advanced chat bubble on the left
            convertView = messageInflater.inflate(R.layout.their_audio_message, parent, false);
            convertView.setTag(holder);

            holder.avatar = convertView.findViewById(R.id.avatar);
            holder.name = convertView.findViewById(R.id.name);
            holder.seekBody = convertView.findViewById(R.id.seek_bar);
            holder.mButtonPlay = convertView.findViewById(R.id.btn_play);
            holder.timePass = convertView.findViewById(R.id.timeView);
            holder.timePass.setText("0:00");
            holder.name.setText(message.getFriendID());

            holder.messageBody = message.getMedia();
            holder.rl = convertView.findViewById(R.id.their_relative);
        }

        holder.rl.setOnClickListener(view -> {
            if(isExpanded) ((ExpandableListView) parent).collapseGroup(i);
            else ((ExpandableListView) parent).expandGroup(i, true);
        });

        holder.mButtonPlay.setOnClickListener(view -> {
            // If media player another instance already running then stop it first
            if (holder.messageBody == null) {
                holder.messageBody = message.getMedia();
            }
            if(holder.messageBody.isPlaying()){
                if(SearchChat.theme_boolean) {
                    holder.mButtonPlay.setImageResource(R.drawable.pause_white);
                }else{
                    holder.mButtonPlay.setImageResource(R.drawable.pause);
                }
                //mPlayer.stop();
                holder.messageBody.pause();
            }else{
                if(SearchChat.theme_boolean) {
                    holder.mButtonPlay.setImageResource(R.drawable.play_logo_white3);
                }else{
                    holder.mButtonPlay.setImageResource(R.drawable.play_logo3);
                }

                holder.messageBody.start();

                holder.seekBody.setMax(holder.messageBody.getDuration()/1000);

                mRunnable = () -> {
                    if(holder.messageBody!=null){
                        int mCurrentPosition = holder.messageBody.getCurrentPosition()/1000; // In milliseconds
                        holder.seekBody.setProgress(mCurrentPosition);
                        int duration  = holder.messageBody.getDuration()/1000; // In milliseconds
                        int due = (holder.messageBody.getDuration() - holder.messageBody.getCurrentPosition())/1000;
                        int pass = duration - due;
                        int reminder = pass%60;
                        String zero;
                        if(reminder<10){
                            zero = "0";
                        }else{
                            zero = "";
                        }
                        holder.timePass.setText(pass/60+":"+zero+pass%60);
                    }
                    mHandler.postDelayed(mRunnable,1000);
                };
                mHandler.postDelayed(mRunnable,1000);
                //getAudioStats(holder.messageBody);
                //initializeSeekBar();
            }

        });

        holder.seekBody.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(holder.messageBody!=null && b){
                    holder.messageBody.seekTo(i*1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}

class MessageViewHolderExt {
    public RelativeLayout rl;
    public View avatar;
    public TextView name;
    public MediaPlayer messageBody;
    public SeekBar seekBody;
    public FloatingActionButton mButtonPlay;
    public TextView timePass;
}
