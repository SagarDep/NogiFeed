package shts.jp.android.nogifeed.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import shts.jp.android.nogifeed.R;
import shts.jp.android.nogifeed.fragments.MemberDetailFragment;
import shts.jp.android.nogifeed.models.Member;

public class MemberDetailActivity extends AppCompatActivity {

    private static final String TAG = MemberDetailActivity.class.getSimpleName();

    public static Intent getStartIntent(Context context, Member member) {
        return getStartIntent(context, member.getId());
    }

    public static Intent getStartIntent(Context context, int memberId) {
        return new Intent(context, MemberDetailActivity.class)
                .putExtra("memberId", memberId);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_detail2);

        MemberDetailFragment memberDetailFragment
                = MemberDetailFragment.newInstance(getIntent().getIntExtra("memberId", -1));

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, memberDetailFragment, MemberDetailFragment.class.getSimpleName());
        ft.commit();
    }

}
