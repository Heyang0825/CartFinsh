package com.jueyes.shoppingcartdemo;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MainAdapter.Callback {

    private RecyclerView recyclerView_Content;
    private TextView textView_Edit;
    private LinearLayout linearLayout_ChoiceShop;
    private LinearLayout linearLayout_ChoiceEdit;
    private ImageView imageView_ChoiceShop;
    private ImageView imageView_ChoiceEdit;
    private TextView textView_Price;
    private Button button_Settlement;
    private Button button_Delete;
    private Button button_Clear;
    private MainAdapter adapter;

    private int choiceStart = 0;    // 选中状态(0:结算模式;1:编辑模式)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取控件
        recyclerView_Content = findViewById(R.id.recyclerView_MainActivity_Content);
        textView_Edit = findViewById(R.id.textView_MainActivity_Edit);
        linearLayout_ChoiceShop = findViewById(R.id.linearLayout_MainActivity_ChoiceShop);
        linearLayout_ChoiceEdit = findViewById(R.id.linearLayout_MainActivity_ChoiceEdit);
        imageView_ChoiceShop = findViewById(R.id.imageView_MainActivity_ChoiceShop);
        imageView_ChoiceEdit = findViewById(R.id.imageView_MainActivity_ChoiceEdit);
        textView_Price = findViewById(R.id.textView_MainActivity_Price);
        button_Settlement = findViewById(R.id.button_MainActivity_Settlement);
        button_Delete = findViewById(R.id.button_MainActivity_Delete);
        button_Clear = findViewById(R.id.button_MainActivity_Clear);

        // 初始化选择状态
        if (choiceStart == 0) {
            textView_Edit.setText("编辑");
            linearLayout_ChoiceShop.setVisibility(View.VISIBLE);
            linearLayout_ChoiceEdit.setVisibility(View.GONE);
        } else {
            textView_Edit.setText("保存");
            linearLayout_ChoiceShop.setVisibility(View.GONE);
            linearLayout_ChoiceEdit.setVisibility(View.VISIBLE);
        }

        // 编辑按钮点击事件
        textView_Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choiceStart = choiceStart == 0 ? 1 : 0;
                // 初始化选择状态
                if (choiceStart == 0) {
                    textView_Edit.setText("编辑");
                    linearLayout_ChoiceShop.setVisibility(View.VISIBLE);
                    linearLayout_ChoiceEdit.setVisibility(View.GONE);
                } else {
                    textView_Edit.setText("保存");
                    linearLayout_ChoiceShop.setVisibility(View.GONE);
                    linearLayout_ChoiceEdit.setVisibility(View.VISIBLE);
                }
            }
        });

        // 全选按钮点击事件
        imageView_ChoiceShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter != null) adapter.allChoice();
            }
        });
        imageView_ChoiceEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter != null) adapter.allChoice();
            }
        });

        // 结算按钮点击事件
        button_Settlement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter == null) return;
                ArrayList<MainAdapter.WaresBean> waresList = adapter.getChoiceWares();
                if (waresList == null || waresList.size() < 1) {
                    Toast.makeText(MainActivity.this, "请选择商品!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "结算" + waresList.size() + "件商品", Toast.LENGTH_SHORT).show();
                    // TODO: 2018/12/3 这里获去所有商品ID,然后进入结算页面
                }
            }
        });

        // 删除按钮点击事件
        button_Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter == null) return;
                ArrayList<MainAdapter.WaresBean> waresList = adapter.getChoiceWares();
                if (waresList == null || waresList.size() < 1) {
                    Toast.makeText(MainActivity.this, "请选择商品!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "删除" + waresList.size() + "件商品", Toast.LENGTH_SHORT).show();
                    // TODO: 2018/12/3 根据数据删除多个商品,向后台发送数据,删除成功后刷新页面数据
                    getCartData();
                }
            }
        });

        // 清理按钮点击事件
        button_Clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter == null) return;
                ArrayList<MainAdapter.WaresBean> waresList = adapter.getLoseWares();
                if (waresList == null || waresList.size() < 1) {
                    Toast.makeText(MainActivity.this, "没有失效商品!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "清理" + waresList.size() + "件失效商品", Toast.LENGTH_SHORT).show();
                    // TODO: 2018/12/3 根据数据删除多个商品,向后台发送数据,删除成功后刷新页面数据
                    getCartData();
                }
            }
        });

        // 初始化 RecyclerView
        recyclerView_Content.setLayoutManager(new LinearLayoutManager(this));
        recyclerView_Content.setAdapter(adapter = new MainAdapter(this));

        getCartData();
    }

    private String image = "https://image.qqsk.com/productImage/1536905504213.jpg";

    public void getCartData() {

        // 从服务器获取的原始数据需要整理变成新的数据,设置给Adapter
        ArrayList<MainAdapter.WaresBean> waresList = new ArrayList<>();
        waresList.add(new MainAdapter.WaresBean(1, "商品名称 店铺1 商品1 未失效", image, 1, "店铺1", 1.1f, 1.2f, false, 1));
        waresList.add(new MainAdapter.WaresBean(2, "商品名称 店铺1 商品2 未失效", image, 1, "店铺1", 2.1f, 2.2f, false, 2));
        waresList.add(new MainAdapter.WaresBean(3, "商品名称 店铺1 商品3 已失效", image, 1, "店铺1", 3.1f, 3.2f, true, 3));
        waresList.add(new MainAdapter.WaresBean(4, "商品名称 店铺2 商品4 未失效", image, 2, "店铺2", 4.1f, 4.2f, false, 4));
        waresList.add(new MainAdapter.WaresBean(5, "商品名称 店铺2 商品5 未失效", image, 2, "店铺2", 5.1f, 5.2f, false, 5));
        waresList.add(new MainAdapter.WaresBean(6, "商品名称 店铺2 商品6 已失效", image, 2, "店铺2", 6.1f, 6.2f, true, 6));
        waresList.add(new MainAdapter.WaresBean(7, "商品名称 店铺3 商品7 未失效", image, 3, "店铺3", 7.1f, 7.2f, false, 7));
        waresList.add(new MainAdapter.WaresBean(8, "商品名称 店铺3 商品8 未失效", image, 3, "店铺3", 8.1f, 8.2f, false, 8));
        waresList.add(new MainAdapter.WaresBean(9, "商品名称 店铺3 商品9 已失效", image, 3, "店铺3", 9.1f, 9.2f, true, 9));
        adapter.setWaresList(waresList);
    }

    /**
     * 商品选中状态改变
     *
     * @param isAllElection 是否全部选择
     * @param waresList     当前选中的商品列表
     */
    @Override
    public void onChoiceChange(boolean isAllElection, ArrayList<MainAdapter.WaresBean> waresList) {
        // 修改全选按钮状态
        imageView_ChoiceShop.setImageResource(isAllElection ? R.drawable.choice_yes : R.drawable.choice_no);
        imageView_ChoiceEdit.setImageResource(isAllElection ? R.drawable.choice_yes : R.drawable.choice_no);

        // 修改价格标签
        float price = 0;
        for (MainAdapter.WaresBean waresBean : waresList) {
            price += (waresBean.getWaresNewPrice() * waresBean.getWaresNum());
        }
        textView_Price.setText("合计: ¥" + price);

        // 修改结算按钮状态
        button_Settlement.setBackgroundColor(waresList.size() < 1 ? Color.parseColor("#F5F5F5") : Color.parseColor("#FF0000"));
        button_Settlement.setText(waresList.size() < 1 ? "结算" : "结算(" + waresList.size() + ")");
        button_Settlement.setTextColor(waresList.size() < 1 ? Color.parseColor("#121212") : Color.parseColor("#FFFFFF"));
    }

    /**
     * 删除某一件商品(包含失效商品)
     *
     * @param waresBean 商品数据
     */
    @Override
    public void onDeleteWares(MainAdapter.WaresBean waresBean) {
        Toast.makeText(this, "删除商品:" + waresBean.getWaresName(), Toast.LENGTH_SHORT).show();
        // TODO: 2018/12/3 根据删除的商品信息,想后台提交数据,成功后刷新界面
        getCartData();
    }

    /**
     * 清理全部失效商品
     *
     * @param waresList
     */
    @Override
    public void onClearWares(ArrayList<MainAdapter.WaresBean> waresList) {
        Toast.makeText(this, "清理" + waresList.size() + "件商品", Toast.LENGTH_SHORT).show();
        // TODO: 2018/12/3 根据删除的商品信息,向后台提交数据,成功后刷新界面
        getCartData();
    }
}
