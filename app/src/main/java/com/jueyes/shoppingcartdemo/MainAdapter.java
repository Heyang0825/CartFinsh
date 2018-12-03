package com.jueyes.shoppingcartdemo;

import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_STORE = 0;
    public static final int TYPE_WARES = 1;
    public static final int TYPE_LOST_TITLE = 2;
    public static final int TYPE_LOST_WARES = 3;
    public static final int TYPE_EMPTY = 4;

    private ArrayList<ItemBean> itemBeanList;
    private Callback callback;

    public MainAdapter(Callback callback) {
        this.callback = callback;
    }

    /**
     * 设置商品数据
     *
     * @param waresList
     */
    public void setWaresList(ArrayList<WaresBean> waresList) {
        TreeMap<Integer, ArrayList<WaresBean>> storeWaresMap = null; // 店铺(ID)商品集合
        ArrayList<WaresBean> lostWaresList = null;                   // 失效商品集合
        // 防止传入空数据
        if (waresList == null) waresList = new ArrayList<>();
        // 对数据集合进行遍历
        for (WaresBean waresBean : waresList) {
            // 判断商品是否失效,如果是失效商品就添加到集合中
            if (waresBean.isWaresLoseState()) {
                if (lostWaresList == null) lostWaresList = new ArrayList<>();
                lostWaresList.add(waresBean);
            } else {
                // 防止集合为null
                if (storeWaresMap == null) storeWaresMap = new TreeMap<>();

                // 检测当前店铺是否添加到店铺商品集合(storeWaresList)中
                if (!storeWaresMap.containsKey(waresBean.getWaresStoreID())) {
                    // 添加商铺到Map集合中
                    storeWaresMap.put(waresBean.getWaresStoreID(), new ArrayList<WaresBean>());
                }

                // 将商品添加到店铺商品集合(storeWaresList)中相应的店铺下面
                storeWaresMap.get(waresBean.getWaresStoreID()).add(waresBean);
            }
        }

        itemBeanList = new ArrayList<>(); // 数据集合

        // 遍历店铺集合
        for (Map.Entry<Integer, ArrayList<WaresBean>> entry : storeWaresMap.entrySet()) {
            // 添加店铺到itemBeanList集合中
            if (entry.getValue() != null && entry.getValue().size() > 0) {
                itemBeanList.add(new ItemBean(TYPE_STORE, new StoreBean(entry.getValue().get(0).getWaresStoreID(), entry.getValue().get(0).getWaresStoreName())));
            }

            // 遍历店铺下的商品集合,并添加到itemBeanList集合中
            for (WaresBean waresBean : entry.getValue()) {
                itemBeanList.add(new ItemBean(TYPE_WARES, waresBean));
            }
        }

        // 添加失效商品到itemBeanList集合中
        if (lostWaresList != null && lostWaresList.size() > 0) {
            itemBeanList.add(new ItemBean(TYPE_LOST_TITLE, null));
            for (WaresBean waresBean : lostWaresList) {
                itemBeanList.add(new ItemBean(TYPE_LOST_WARES, waresBean));
            }
        }

        // 更新数据
        notifyDataSetChanged();
        onChoiceChange();   // 选中状态改变
    }

    /**
     * 全选(不包括失效商品)
     * 如果当前已经全选,则进行反选操作
     */
    public void allChoice() {
        if (itemBeanList == null) return;

        boolean choice = false;
        for (ItemBean itemBean : itemBeanList) {
            if (itemBean.getItemData() instanceof WaresBean && !((WaresBean) itemBean.getItemData()).isWaresLoseState() && !((WaresBean) itemBean.getItemData()).isChoiceState()) {
                choice = true;
            }
        }
        for (ItemBean itemBean : itemBeanList) {
            if (itemBean.getItemData() instanceof WaresBean) {
                ((WaresBean) itemBean.getItemData()).setChoiceState(choice);
            }
        }
        notifyDataSetChanged();
        onChoiceChange();   // 选中状态改变
    }

    /**
     * 获取当前选中的商品,不包含失效商品
     * 如果当前未选中任何商品,返回空集合(不为null)
     *
     * @return
     */
    public ArrayList<WaresBean> getChoiceWares() {
        ArrayList<WaresBean> waresList = new ArrayList<>();
        for (ItemBean itemBean : itemBeanList) {
            if (itemBean.getItemData() instanceof WaresBean && !((WaresBean) itemBean.getItemData()).isWaresLoseState() && ((WaresBean) itemBean.getItemData()).isChoiceState()) {
                waresList.add((WaresBean) itemBean.getItemData());
            }
        }
        return waresList;
    }

    /**
     * 返回全部失效商品
     *
     * @return
     */
    public ArrayList<WaresBean> getLoseWares() {
        ArrayList<WaresBean> waresList = new ArrayList<>();
        for (ItemBean itemBean : itemBeanList) {
            if (itemBean.getItemData() instanceof WaresBean && ((WaresBean) itemBean.getItemData()).isWaresLoseState()) {
                waresList.add((WaresBean) itemBean.getItemData());
            }
        }
        return waresList;
    }

    /**
     * 删除某一个商品(包含失效商品)
     *
     * @param wares
     */
    public void deleteWares(WaresBean wares) {
        if (callback != null) callback.onDeleteWares(wares);
    }

    /**
     * 清理全部失效商品
     */
    public void onClearWares() {
        if (callback != null) callback.onClearWares(getLoseWares());
    }

    /**
     * 通知回调接口,选中状态改变
     */
    private void onChoiceChange() {
        if (callback == null) return;
        boolean isAllElection = true;
        ArrayList<WaresBean> waresList = new ArrayList<>();
        for (ItemBean itemBean : itemBeanList) {
            if (itemBean.getItemData() instanceof WaresBean && !((WaresBean) itemBean.getItemData()).isWaresLoseState()) {
                if (!((WaresBean) itemBean.getItemData()).isChoiceState()) {
                    isAllElection = false;
                } else {
                    waresList.add((WaresBean) itemBean.getItemData());
                }
            }
        }

        // 通知回调数据
        callback.onChoiceChange(isAllElection, waresList);
    }

    @Override
    public int getItemCount() {
        return itemBeanList == null ? 1 : itemBeanList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return itemBeanList != null && itemBeanList.size() > position ? itemBeanList.get(position).getItemType() : TYPE_EMPTY;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case TYPE_STORE:
                return StoreViewHolder.instance(viewGroup);
            case TYPE_WARES:
                return WaresViewHolder.instance(viewGroup);
            case TYPE_LOST_TITLE:
                return LostTitleViewHolder.instance(viewGroup);
            case TYPE_LOST_WARES:
                return LostWaresViewHolder.instance(viewGroup);
            case TYPE_EMPTY:
                return EmptyViewHolder.instance(viewGroup);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        if (viewHolder instanceof StoreViewHolder && itemBeanList.get(position).getItemData() instanceof StoreBean)
            ((StoreViewHolder) viewHolder).onBindView((StoreBean) itemBeanList.get(position).getItemData(), itemBeanList, this);

        if (viewHolder instanceof WaresViewHolder && itemBeanList.get(position).getItemData() instanceof WaresBean)
            ((WaresViewHolder) viewHolder).onBindView((WaresBean) itemBeanList.get(position).getItemData(), this);

        if (viewHolder instanceof LostTitleViewHolder)
            ((LostTitleViewHolder) viewHolder).onBindView(this);

        if (viewHolder instanceof LostWaresViewHolder && itemBeanList.get(position).getItemData() instanceof WaresBean)
            ((LostWaresViewHolder) viewHolder).onBindView((WaresBean) itemBeanList.get(position).getItemData(), this);
    }

    // 店铺模式选中
    public void onStoreChoice(int storeID, boolean isChoice) {
        for (ItemBean itemBean : itemBeanList) {
            if (itemBean.getItemData() instanceof WaresBean && ((WaresBean) itemBean.getItemData()).getWaresStoreID() == storeID) {
                ((WaresBean) itemBean.getItemData()).setChoiceState(isChoice);
            }
        }
        notifyDataSetChanged();
        onChoiceChange();   // 选中状态改变
    }

    // 商品结算模式选中
    public void onWaresChoice() {
        notifyDataSetChanged();
        onChoiceChange();   // 选中状态改变
    }

    // 店铺名称 ViewHolder
    public static class StoreViewHolder extends RecyclerView.ViewHolder {

        private TextView textView_Title;
        private ImageView imageView_Choice;

        // 获取 StoreViewHolder
        public static StoreViewHolder instance(ViewGroup parent) {
            return new StoreViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_store, parent, false));
        }

        // 构造函数
        private StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            textView_Title = itemView.findViewById(R.id.textView_StoreHolder_Title);
            imageView_Choice = itemView.findViewById(R.id.imageView_StoreHolder_Choice);
        }

        // 绑定数据
        public void onBindView(final StoreBean storeBean, ArrayList<ItemBean> itemBeanList, final MainAdapter adapter) {
            textView_Title.setText(storeBean.getStoreName());

            // 初始化结算模式选中状态
            boolean start = true;
            for (ItemBean itemBean : itemBeanList) {
                if (itemBean.getItemData() instanceof WaresBean && ((WaresBean) itemBean.getItemData()).getWaresStoreID() == storeBean.getStoreID() && !((WaresBean) itemBean.getItemData()).isWaresLoseState() && !((WaresBean) itemBean.getItemData()).isChoiceState()) {
                    start = false;
                }
            }
            imageView_Choice.setImageResource(start ? R.drawable.choice_yes : R.drawable.choice_no);
            // 设置选中按钮点击事件
            final boolean finalStart = start;
            imageView_Choice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapter != null)
                        adapter.onStoreChoice(storeBean.getStoreID(), !finalStart);
                }
            });
        }
    }

    // 商品详情 ViewHolder
    public static class WaresViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout relativeLayout_Content;
        private TextView textView_Delete;
        private ImageView imageView_Choice;
        private TextView textView_Title;
        private ImageView imageView_Icon;
        private TextView textView_PriceA;
        private TextView textView_PriceB;

        private TextView textView_NumberReduce;
        private TextView textView_Number;
        private TextView textView_NumberPlus;

        // 获取 StoreViewHolder
        public static WaresViewHolder instance(ViewGroup parent) {
            return new WaresViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_wares_menu, parent, false));
        }

        // 构造函数
        private WaresViewHolder(@NonNull View itemView) {
            super(itemView);
            relativeLayout_Content = itemView.findViewById(R.id.relativeLayout_WaresHolder_Content);
            textView_Delete = itemView.findViewById(R.id.textView_WaresHolder_Delete);
            imageView_Choice = itemView.findViewById(R.id.imageView_WaresHolder_Choice);
            textView_Title = itemView.findViewById(R.id.textView_WaresHolder_Title);
            imageView_Icon = itemView.findViewById(R.id.imageView_WaresHolder_Icon);
            textView_PriceA = itemView.findViewById(R.id.textView_WaresHolder_PriceA);
            textView_PriceB = itemView.findViewById(R.id.textView_WaresHolder_PriceB);

            textView_NumberReduce = itemView.findViewById(R.id.textView_WaresHolder_NumberReduce);
            textView_Number = itemView.findViewById(R.id.textView_WaresHolder_Number);
            textView_NumberPlus = itemView.findViewById(R.id.textView_WaresHolder_NumberPlus);
        }

        // 绑定数据
        public void onBindView(final WaresBean waresBean, final MainAdapter adapter) {
            relativeLayout_Content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 必须设置点击事件,否则侧滑菜单失效,原因未知
                    // TODO: 2018/12/3 点击进入详情页
                }
            });
            // 删除按钮
            textView_Delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.deleteWares(waresBean);
                }
            });

            textView_Title.setText(waresBean.getWaresName());
            Glide.with(imageView_Icon).load(waresBean.getWaresIcon()).into(imageView_Icon);
            textView_PriceA.setText("¥" + waresBean.getWaresNewPrice());
            textView_PriceB.setText("¥" + waresBean.getWaresOldPrice());
            textView_PriceB.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);

            imageView_Choice.setImageResource(waresBean.isChoiceState() ? R.drawable.choice_yes : R.drawable.choice_no);
            imageView_Choice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapter != null) {
                        waresBean.setChoiceState(!waresBean.isChoiceState());
                        adapter.onWaresChoice();
                    }
                }
            });

            // 初始化数量
            textView_Number.setText("" + waresBean.getWaresNum());
            textView_NumberReduce.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    waresBean.setWaresNum(waresBean.getWaresNum() == 1 ? 1 : waresBean.getWaresNum() - 1);
                    textView_Number.setText("" + waresBean.getWaresNum());
                    adapter.onChoiceChange();
                }
            });
            textView_NumberPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    waresBean.setWaresNum(waresBean.getWaresNum() + 1);
                    textView_Number.setText("" + waresBean.getWaresNum());
                    adapter.onChoiceChange();
                }
            });
        }
    }

    // 失效商品标题 ViewHolder
    public static class LostTitleViewHolder extends RecyclerView.ViewHolder {
        private Button button_Clear;

        // 获取 StoreViewHolder
        public static LostTitleViewHolder instance(ViewGroup parent) {
            return new LostTitleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_lost_title, parent, false));
        }

        // 构造函数
        private LostTitleViewHolder(@NonNull View itemView) {
            super(itemView);
            button_Clear = itemView.findViewById(R.id.button_LostWaresHolder_Clear);
        }

        // 绑定数据
        public void onBindView( final MainAdapter adapter) {
            button_Clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.onClearWares();
                }
            });
        }

    }

    // 失效商品详情 ViewHolder
    public static class LostWaresViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout relativeLayout_Content;
        private TextView textView_Delete;
        private TextView textView_Title;
        private ImageView imageView_Icon;
        private TextView textView_Num;

        // 获取 StoreViewHolder
        public static LostWaresViewHolder instance(ViewGroup parent) {
            return new LostWaresViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_lost_wares_menu, parent, false));
        }

        // 构造函数
        private LostWaresViewHolder(@NonNull View itemView) {
            super(itemView);
            relativeLayout_Content = itemView.findViewById(R.id.relativeLayout_LostWaresHolder_Content);
            textView_Delete = itemView.findViewById(R.id.textView_LostWaresHolder_Delete);
            textView_Title = itemView.findViewById(R.id.textView_LostWaresHolder_Title);
            imageView_Icon = itemView.findViewById(R.id.imageView_LostWaresHolder_Icon);
            textView_Num = itemView.findViewById(R.id.textView_LostWaresHolder_Num);
        }

        // 绑定数据
        public void onBindView(final WaresBean waresBean, final MainAdapter adapter) {
            relativeLayout_Content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 必须设置点击事件,否则侧滑菜单失效,原因未知
                    // TODO: 2018/12/3 点击进入详情页
                }
            });

            // 删除按钮
            textView_Delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.deleteWares(waresBean);
                }
            });

            textView_Title.setText(waresBean.getWaresName());
            Glide.with(imageView_Icon).load(waresBean.getWaresIcon()).into(imageView_Icon);
            textView_Num.setText("X " + waresBean.getWaresNum());
        }
    }

    // 空界面ViewHolder
    public static class EmptyViewHolder extends RecyclerView.ViewHolder {

        // 获取 StoreViewHolder
        public static EmptyViewHolder instance(ViewGroup parent) {
            return new EmptyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_empty, parent, false));
        }

        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }


    // Item类型和数据封装类
    public static class ItemBean {
        private int itemType;       // Item 类型
        private Object itemData;    // Item 数据

        public ItemBean(int itemType, Object itemData) {
            this.itemType = itemType;
            this.itemData = itemData;
        }

        public int getItemType() {
            return itemType;
        }

        public Object getItemData() {
            return itemData;
        }
    }

    // 商店数据
    public static class StoreBean {
        private int storeID;            // 店铺标识(作为店铺的唯一标识,同时用于判断商品属于哪个店铺的)
        private String storeName;       // 店铺名称

        public StoreBean(int storeID, String storeName) {
            this.storeID = storeID;
            this.storeName = storeName;
        }

        public int getStoreID() {
            return storeID;
        }

        public void setStoreID(int storeID) {
            this.storeID = storeID;
        }

        public String getStoreName() {
            return storeName;
        }

        public void setStoreName(String storeName) {
            this.storeName = storeName;
        }
    }

    // 商品数据
    public static class WaresBean {
        private int waresID;                // 商品标识
        private String waresName;           // 商品名称
        private String waresIcon;           // 商品图标
        private float waresNewPrice;        // 商品当前价格
        private float waresOldPrice;        // 商品原始价格
        private int waresStoreID;           // 商品店铺标识(作为店铺的唯一标识,同时用于判断商品属于哪个店铺的)
        private String waresStoreName;      // 商品店铺名称
        private int waresNum;               // 商品数量(最小为1)
        private boolean waresChoiceState;   // 选中状态
        private boolean waresLoseState;     // 失效状态(true 为已失效)

        public WaresBean(int waresID, String waresName, String waresIcon, int waresStoreID, String waresStoreName, float waresNewPrice, float waresOldPrice, boolean waresLoseState, int waresNum) {
            this.waresID = waresID;
            this.waresName = waresName;
            this.waresIcon = waresIcon;
            this.waresStoreID = waresStoreID;
            this.waresStoreName = waresStoreName;
            this.waresNewPrice = waresNewPrice;
            this.waresOldPrice = waresOldPrice;
            this.waresLoseState = waresLoseState;
            this.waresNum = waresNum;
        }

        public int getWaresID() {
            return waresID;
        }

        public void setWaresID(int waresID) {
            this.waresID = waresID;
        }

        public String getWaresName() {
            return waresName;
        }

        public void setWaresName(String waresName) {
            this.waresName = waresName;
        }

        public String getWaresIcon() {
            return waresIcon;
        }

        public void setWaresIcon(String waresIcon) {
            this.waresIcon = waresIcon;
        }

        public float getWaresNewPrice() {
            return waresNewPrice;
        }

        public void setWaresNewPrice(float waresNewPrice) {
            this.waresNewPrice = waresNewPrice;
        }

        public float getWaresOldPrice() {
            return waresOldPrice;
        }

        public void setWaresOldPrice(float waresOldPrice) {
            this.waresOldPrice = waresOldPrice;
        }

        public int getWaresStoreID() {
            return waresStoreID;
        }

        public void setWaresStoreID(int waresStoreID) {
            this.waresStoreID = waresStoreID;
        }

        public String getWaresStoreName() {
            return waresStoreName;
        }

        public void setWaresStoreName(String waresStoreName) {
            this.waresStoreName = waresStoreName;
        }

        public int getWaresNum() {
            return waresNum;
        }

        public void setWaresNum(int waresNum) {
            this.waresNum = waresNum;
        }

        public boolean isChoiceState() {
            return waresChoiceState;
        }

        public void setChoiceState(boolean waresChoiceState) {
            this.waresChoiceState = waresChoiceState;
        }

        public boolean isWaresLoseState() {
            return waresLoseState;
        }

        public void setWaresLoseState(boolean waresLoseState) {
            this.waresLoseState = waresLoseState;
        }
    }

    // 回调接口
    public interface Callback {
        /**
         * 商品选中状态改变
         *
         * @param isAllElection 是否全部选择
         * @param waresList     当前选中的商品列表
         */
        void onChoiceChange(boolean isAllElection, ArrayList<WaresBean> waresList);

        /**
         * 删除某一件商品(包含失效商品)
         *
         * @param waresBean 商品数据
         */
        void onDeleteWares(WaresBean waresBean);

        /**
         * 清理全部失效商品
         */
        void onClearWares(ArrayList<WaresBean> waresList);

    }
}