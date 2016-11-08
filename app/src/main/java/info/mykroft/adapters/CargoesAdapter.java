package info.mykroft.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import info.mykroft.R;
import info.mykroft.models.Cargo;
import info.mykroft.views.activities.CargoesOnTrainActivity;

/**
 * Created by MyKroft on 11/1/2016.
 */

public class CargoesAdapter extends RecyclerView.Adapter<CargoesAdapter.ViewHolder> {
    private ArrayList<Cargo> cargoes = new ArrayList<>();
    private CargoesOnTrainActivity activity;

    public CargoesAdapter(CargoesOnTrainActivity activity, ArrayList<Cargo> cargoes) {
        this.cargoes = cargoes;
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.cargo_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.uniqueId.setText("Cargo Id: " + cargoes.get(position).getUniqueId());
        holder.monitoringOfficer.setText("Loader Name: " + cargoes.get(position).getMonitoringOfficer());
        holder.customerName.setText("Customer Name: " + cargoes.get(position).getCustomerName());
        holder.goodsType.setText("Goods type: " + cargoes.get(position).getGoodsType());
        holder.qty.setText("Qty: " + cargoes.get(position).getQty());

    }

    @Override
    public int getItemCount() {
        return cargoes.size();
    }

    public void addData(ArrayList<Cargo> cargoes) {
        this.cargoes = cargoes;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView uniqueId;
        TextView qty;
        TextView goodsType;
        TextView customerName;
        TextView monitoringOfficer;


        public ViewHolder(View itemView) {
            super(itemView);
            uniqueId = (TextView) itemView.findViewById(R.id.cargo_id);
            qty = (TextView) itemView.findViewById(R.id.qty);
            goodsType = (TextView) itemView.findViewById(R.id.goods_type);
            customerName = (TextView) itemView.findViewById(R.id.customer_name);
            monitoringOfficer = (TextView) itemView.findViewById(R.id.monitoring_officer);

        }
    }
}
