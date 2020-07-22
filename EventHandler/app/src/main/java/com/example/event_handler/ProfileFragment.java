package com.example.event_handler;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;

public class FriendsFragment extends Fragment{
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_friends, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		//super.onViewCreated(view, savedInstanceState);
		User simpleUserData = new User("Jovana","Jovanovic","jovanajovanovic@gmail.com","Jovanaaa","0324534553");
		ArrayList<User> listUsers = new ArrayList<User>();
		for(int i=0;i<5;i++) {
			listUsers.add(new User(simpleUserData));
		}
		AdapterFriends af = new AdapterFriends(getActivity(),listUsers);
		GridView viewUsers = view.findViewById(R.id.grid);
		viewUsers.setAdapter(af);
	}
}
