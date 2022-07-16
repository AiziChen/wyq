package com.cozz.wyq;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.cozz.wyq.databinding.ActivityMainBinding;
import com.cozz.wyq.pojo.Group;
import com.cozz.wyq.tools.DiskTool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 0x6597FFBC;
    public static final String GROUPS_CONFIG_FILE = "groups.json";
    public static final String LAST_SELECTED_GROUP_NAME = "last-selected-group-name.txt";

    private ActivityMainBinding binding;
    private List<Group> groups;
    private List<String> entries;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        permissionsRequest();

        String groupJson = DiskTool.readFile(GROUPS_CONFIG_FILE);
        if (TextUtils.isEmpty(groupJson)) {
            DiskTool.replaceFile("[]", GROUPS_CONFIG_FILE);
            groupJson = "[]";
        }
        try {
            groups = JSONObject.parseArray(groupJson, Group.class);
        } catch (RuntimeException e) {
            Toast.makeText(getApplicationContext(), "读取`" + GROUPS_CONFIG_FILE + "`文件失败", Toast.LENGTH_SHORT).show();
            return;
        }

        entries = new ArrayList<>();
        entries.add("请选择一个群组名");
        for (Group group : groups) {
            entries.add(group.getGroupName());
        }
        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, entries);
        binding.spGroupNameSelector.setAdapter(adapter);
        binding.spGroupNameSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 0) {
                    loadCurrentGroup(i);
                } else {
                    String lastSelectedGroupName = DiskTool.readFirstLine(LAST_SELECTED_GROUP_NAME);
                    for (int q = 1; q < entries.size(); ++q) {
                        if (lastSelectedGroupName != null && lastSelectedGroupName.equals(entries.get(q))) {
                            binding.spGroupNameSelector.setSelection(q);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        binding.btnSave.setOnClickListener(view -> {
            saveCurrentGroup();
        });
        binding.btnDelete.setOnClickListener(view -> {
            String groupName = binding.spGroupNameSelector.getSelectedItem().toString();
            new AlertDialog.Builder(this)
                    .setTitle("删除")
                    .setMessage("删除群组`" + groupName + "`？")
                    .setIcon(R.drawable.ic_baseline_warning_24)
                    .setPositiveButton("确定", (dialogInterface, i) -> {
                        int pos = binding.spGroupNameSelector.getSelectedItemPosition();
                        deleteCurrentGroupInPosition(pos);
                    })
                    .setNegativeButton("取消", null)
                    .setCancelable(false)
                    .show();
        });
    }

    private void loadCurrentGroup(int pos) {
        for (Group group : groups) {
            if (group.getGroupName().equals(entries.get(pos))) {
                binding.etGroupName.setText(group.getGroupName());
                binding.etGroupId.setText(group.getGroupId());
                binding.etDelayStart.setText(group.getDelayStart() + "");
                binding.etDelayEnd.setText(group.getDelayEnd() + "");
                binding.etUserId1.setText(group.getUserId1());
                binding.etUserId2.setText(group.getUserId2());
                binding.switchEnable.setChecked(group.isEnabled());

                StringBuilder sb = new StringBuilder();
                for (String line : group.getMsgs()) {
                    sb.append(line).append('\n');
                }
                binding.etMsgs.setText(sb.toString());

                break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        String groupName = binding.spGroupNameSelector.getSelectedItem().toString();
        DiskTool.replaceFile(groupName, LAST_SELECTED_GROUP_NAME);
    }

    private void permissionsRequest() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "请先授权本APP`显示在其它应用上层`权限", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + this.getPackageName()));
            startActivityForResult(intent, PERMISSION_REQUEST_CODE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Uri uri = Uri.parse("package:" + getPackageName());
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
                    startActivity(intent);
                } catch (Exception ex) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "授权成功", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "授权失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_add_new_group) {
            int newGroupPos = entries.size();
            String newGroupName = "群组" + newGroupPos;
            Toast.makeText(getApplicationContext(), "新建`" + newGroupName + "`", Toast.LENGTH_SHORT).show();
            createNewGroup(newGroupPos, newGroupName);
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveCurrentGroup() {
        String groupName = binding.etGroupName.getText().toString().trim();
        String groupId = binding.etGroupId.getText().toString().trim();
        String delayStart = binding.etDelayStart.getText().toString().trim();
        String delayEnd = binding.etDelayEnd.getText().toString().trim();
        String msgs = binding.etMsgs.getText().toString().trim();
        boolean checked = binding.switchEnable.isChecked();
        if (TextUtils.isEmpty(groupName)
                || TextUtils.isEmpty(groupId)
                || TextUtils.isEmpty(delayStart)
                || TextUtils.isEmpty(delayEnd)
                || TextUtils.isEmpty(msgs)) {
            Toast.makeText(getApplicationContext(), "除主人模式外的群组内容均不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        Group group = new Group();
        group.setGroupName(groupName);
        group.setGroupId(groupId);
        group.setDelayStart(Integer.parseInt(delayStart));
        group.setDelayEnd(Integer.parseInt(delayEnd));
        List<String> listMsgs = Arrays.asList(msgs.split("\n"));
        group.setMsgs(listMsgs);
        group.setEnabled(checked);

        String userId1 = binding.etUserId1.getText().toString();
        if (!TextUtils.isEmpty(userId1)) {
            group.setUserId1(userId1);
        }
        String userId2 = binding.etUserId2.getText().toString();
        if (!TextUtils.isEmpty(userId2)) {
            group.setUserId2(userId2);
        }
        if (!TextUtils.isEmpty(userId1) || !TextUtils.isEmpty(userId2)) {
            Toast.makeText(getApplicationContext(), "主人模式开启", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "主人模式关闭", Toast.LENGTH_SHORT).show();
        }
        int i = 0;
        for (; i < groups.size(); ++i) {
            if (groups.get(i).getGroupName().equals(group.getGroupName())) {
                groups.remove(i);
                entries.remove(i + 1);
                break;
            }
        }
        groups.add(i, group);
        entries.add(i + 1, group.getGroupName());
        String groupsJsonText = JSONObject.toJSONString(groups);
        // Save the groups data
        boolean rs = DiskTool.replaceFile(groupsJsonText, GROUPS_CONFIG_FILE);
        // Save the current selected group-name
        boolean rs2 = DiskTool.replaceFile(group.getGroupName(), LAST_SELECTED_GROUP_NAME);
        if (rs && rs2) {
            adapter.notifyDataSetChanged();
            Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
            binding.spGroupNameSelector.setSelection(i + 1);
        } else {
            Toast.makeText(getApplicationContext(), "保存失败", Toast.LENGTH_SHORT).show();
            groups.remove(i);
            entries.remove(i + 1);
        }
    }

    private void createNewGroup(int pos, String groupName) {
        entries.add(groupName);
        binding.etGroupName.setText(groupName);
        binding.etGroupId.setText("");
        binding.etDelayStart.setText("");
        binding.etDelayEnd.setText("");
        binding.etUserId1.setText("");
        binding.etUserId2.setText("");
        binding.etMsgs.setText("");
        binding.spGroupNameSelector.setSelection(pos + 1);
        adapter.notifyDataSetChanged();
    }

    private void deleteCurrentGroupInPosition(int pos) {
        entries.remove(pos);
        if (groups.size() >= pos) {
            groups.remove(pos - 1);
        }

        String groupsJsonText = JSONObject.toJSONString(groups);
        // Save the groups data
        boolean rs = DiskTool.replaceFile(groupsJsonText, GROUPS_CONFIG_FILE);
        if (rs) {
            adapter.notifyDataSetChanged();
            Toast.makeText(getApplicationContext(), "删除成功", Toast.LENGTH_SHORT).show();
            binding.spGroupNameSelector.setSelection(pos - 1);
            if (groups.size() >= pos) {
                loadCurrentGroup(pos - 1);
            } else {
                binding.etGroupName.setText(entries.get(pos - 1));
            }
        } else {
            Toast.makeText(getApplicationContext(), "删除失败", Toast.LENGTH_SHORT).show();
        }
    }
}