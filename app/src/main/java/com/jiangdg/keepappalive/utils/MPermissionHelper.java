package com.jiangdg.keepappalive.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/5.
 */

public class MPermissionHelper {
    public static final int REQUEST_CODE_ASK_PERMISSIONS = 22123;
    private List<String> mPermissions;
    private Activity mContext;
    private Dialog setPermissionDialog;
    private Dialog goSettingsDialog;

    public MPermissionHelper(Activity mContext) {
        this.mContext = mContext;
        mPermissions = new ArrayList<>();
    }

    public interface PermissionCallBack {
        void permissionRegisterSuccess(String... permissions);

        void permissionRegisterError(String... permissions);
    }

    /**
     * 检查是否通过了某个权限
     *
     * @param permissions
     * @return
     */
    private boolean hasPermission(String... permissions) {
        mPermissions.clear();
        for (String permission : permissions) {
            int isHasthisPermission = ContextCompat.checkSelfPermission(mContext, permission);
            if (isHasthisPermission != PackageManager.PERMISSION_GRANTED) {
                mPermissions.add(permission);
            }
        }
        return mPermissions.size() == 0;
    }

    /**
     * 请求权限
     *
     * @param permissions
     */
    public void requestPermission(PermissionCallBack callback, String... permissions) {
//        try {
        //首先判断版本是否小于23，小于23则直接通过权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            callback.permissionRegisterSuccess(permissions);
            return;
        }
        //如果没通过权限则申请通过权限
        if (!hasPermission(permissions)) {
            permissions = mPermissions.toArray(new String[mPermissions.size()]);
            //如果检查到设置了不再提醒则需要用户自己去提醒
            if (!shouldShowRequestPermissions(permissions)) {
                //用户自己去提醒
                showMessageOKCancel(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(mContext,
                                mPermissions.toArray(new String[mPermissions.size()]),
                                REQUEST_CODE_ASK_PERMISSIONS);
                    }
                });
                return;
            }
            ActivityCompat.requestPermissions(mContext,
                    mPermissions.toArray(new String[mPermissions.size()]),
                    REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }
        //处理全部通过后的情况
        callback.permissionRegisterSuccess();
//        } catch (RuntimeException e) {
//            //6.0以下手机在没有权限时的提醒
////            callback.permissionRegisterError(mPermissions.toArray(new String[mPermissions.size()]));
//            Toast.makeText(mContext, "检测到您关闭了某些权限，部分功能将无法使用，请您前往应用设置界面来设置权限！", Toast.LENGTH_SHORT).show();
////            e.printStackTrace();
//        }
    }

    private boolean shouldShowRequestPermissions(String... permissions) {
        boolean result = false;
        for (String s : permissions) {
            result = ActivityCompat.shouldShowRequestPermissionRationale(mContext,
                    s);
        }
        return result;
    }

    /**
     * 处理onRequestPermissionsResult
     *
     * @param requestCode
     * @param grantResults
     */
    public void handleRequestPermissionsResult(int requestCode, PermissionCallBack callBack, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                System.out.println("requestCode = " + requestCode);
                boolean allGranted = true;
                for (int grant : grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted && callBack != null) {
                    callBack.permissionRegisterSuccess(mPermissions.toArray(new String[mPermissions.size()]));

                } else if (!allGranted && callBack != null) {
                    callBack.permissionRegisterError(mPermissions.toArray(new String[mPermissions.size()]));
                }
        }
    }

    public void destroy() {
        if (setPermissionDialog != null && setPermissionDialog.isShowing()) {
            setPermissionDialog.dismiss();
            setPermissionDialog = null;
        }
        if (goSettingsDialog != null && goSettingsDialog.isShowing()) {
            goSettingsDialog.dismiss();
            goSettingsDialog = null;
        }

    }

    public void showGoSettingPermissionsDialog(String showPermissionName) {
        goSettingsDialog = new AlertDialog.Builder(mContext).setMessage("检测您已关闭" + showPermissionName + "权限。\n" +
                "部分功能将无法正常使用！\n" +
                "为了保证功能的正常使用，请点击设置->权限管理->打开所需权限！")
                .setTitle("提示")
                .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goPermissionSetting();
                    }
                })
                .setNegativeButton("取消", null)
                .create();
        goSettingsDialog.show();
    }

    public void goPermissionSetting() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        localIntent.setData(Uri.fromParts("package", mContext.getPackageName(), null));
        mContext.startActivity(localIntent);
    }

    private void showMessageOKCancel(DialogInterface.OnClickListener okListener) {
        setPermissionDialog = new AlertDialog.Builder(mContext)
                .setMessage("检测到您有权限未设置，是否确认设置权限？")
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
        ;
        setPermissionDialog.show();
    }

}
