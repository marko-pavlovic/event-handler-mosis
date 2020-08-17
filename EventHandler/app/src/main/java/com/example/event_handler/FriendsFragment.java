package com.example.event_handler;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class FriendsFragment extends Fragment {

	FloatingActionButton fabAddFriend;
	StorageReference mStorageRef;
	private FirebaseUser FBUser;
	private DatabaseReference mDatabaseRef;

	String UserProfilePictureName;
	User temp;
	int friendsCount = 0;
	ArrayList<User> listUsers = new ArrayList<User>();
	Boolean friendsListDone = false;

	FriendList f = FriendList.getInstance();
	ArrayList<String> pom = f.getList();

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_friends, container, false);
		fabAddFriend = view.findViewById(R.id.fabAddFriend);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		//super.onViewCreated(view, savedInstanceState);

		FBUser = FirebaseAuth.getInstance().getCurrentUser();
		mDatabaseRef = FirebaseDatabase.getInstance().getReference();//.child("Friends").child(FBUser.getUid());
		ArrayList<User> listUsers = new ArrayList<User>();
		temp = new User();
		//getFriendsCount();
		//GetFriends();
		Boolean	 asd = true;
		if(true){
			Paper.book("friends").destroy();
		}

		InitFriendsFromDatabase();
		//User simpleUserData = new User("Jovana","Jovanovic","jovanajovanovic@gmail.com","Jovanaaa","0324534553");

		String asdddd = "asds";
		AdapterFriends af = new AdapterFriends(getActivity(),listUsers);
		GridView viewUsers = view.findViewById(R.id.grid);
		viewUsers.setAdapter(af);


		fabAddFriend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Fragment newFragment = null;
				newFragment = (Fragment) new AddFriendFragment();
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				String aggg = "asdsa";
				transaction.replace(R.id.fragment_container, newFragment);
				transaction.addToBackStack(null);

				transaction.commit();
			}
		});
	}


	public void InitFriendsFromDatabase() {
		listUsers.clear();
//		FriendList f = FriendList.getInstance();
//		ArrayList<String> pom = f.getList();
		for (String friend : pom) {
			FirebaseSingleton.getInstance().databaseReference
					.child("Users")
					.child(friend)
					.addListenerForSingleValueEvent(new ValueEventListener() {
						@Override
						public void onDataChange(DataSnapshot dataSnapshot) {
							Object asd = dataSnapshot.getValue();
							User friend = dataSnapshot.getValue(User.class);
							listUsers.add(friend);
							Boolean asdsada = false;
						}
						@Override
						public void onCancelled(DatabaseError databaseError) {
							Boolean asdsasdadada = false;
						}
					});
		}
	}

//	public void getFriendsCount(){
//		mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(FBUser.getUid());
//		String fddf = "ADS";
//		mDatabaseRef.addValueEventListener(new ValueEventListener() {
//			@Override
//			public void onDataChange(DataSnapshot dataSnapshot) {
//				friendsCount = (int) dataSnapshot.getChildrenCount();
//				while(friendsCount > 0)
//					GetUser();
//
//				Toast.makeText(getActivity().getApplicationContext(), "Friends count = " + friendsCount, Toast.LENGTH_LONG).show();
//			}
//
//			@Override
//			public void onCancelled(DatabaseError databaseError) {
//
//			}
//		});
//	}

//	void GetFriends() {
//		FirebaseSingleton.getInstance().databaseReference
//				.child("Friends")
//				.child(FBUser.getUid())
//				.addValueEventListener(new ValueEventListener() {
//					@Override
//					public void onDataChange(DataSnapshot dataSnapshot) {
//						listUsers.clear();
//						for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
//							String friendUID = friendSnapshot.getValue(String.class);
//							friends.add(dataSnapshot.getValue(String.class));
//						}
//
//					}
//
//					@Override
//					public void onCancelled(DatabaseError databaseError) {
//
//					}
//				});
//	}



//	public void GetUser() {
//		mDatabaseRef2 = FirebaseDatabase.getInstance().getReference().child("Friends").child(FBUser.getUid());
//		String fddf = "ADS";
//		mDatabaseRef2.addValueEventListener(new ValueEventListener() {
//			@Override
//			public void onDataChange(DataSnapshot dataSnapshot2) {
//				//User asgggd = new User("Pera","peric","peramail","perica","1234234432","231312312.jpg");
//				//asgggd = (User) dataSnapshot.getValue();
//				Object o = dataSnapshot2.getValue();
//				temp = (User) dataSnapshot2.getValue();
//				listUsers.add(temp);
//				friendsCount--;
//				String qwe = "asdasd";
//
////				String FirstName = dataSnapshot.child("firstName").getValue().toString();
////				String LastName = dataSnapshot.child("lastName").getValue().toString();
////				String UserName = dataSnapshot.child("username").getValue().toString();
////				Object asd = dataSnapshot.getValue();
//				//UserProfilePictureName = dataSnapshot.child("profilePictureStorageName").getValue().toString();
//				//mStorageRef = FirebaseStorage.getInstance().getReference("ProfileImages/" + UserProfilePictureName);
//
//			}
//
//			@Override
//			public void onCancelled(DatabaseError databaseError) {
//				Log.w("asdf", "Greska" + databaseError.toString());
//			}
//		});
//	}






}
