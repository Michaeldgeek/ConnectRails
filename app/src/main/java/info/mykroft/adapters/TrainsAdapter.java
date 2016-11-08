package info.mykroft.adapters;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import info.mykroft.R;
import info.mykroft.models.CurrentLocation;
import info.mykroft.models.Train;
import info.mykroft.utils.Constants;
import info.mykroft.views.activities.CargoActivity;
import info.mykroft.views.activities.MapsActivity;
import info.mykroft.views.activities.TrainsActivity;

/**
 * Created by MyKroft on 11/1/2016.
 */

public class TrainsAdapter extends RecyclerView.Adapter<TrainsAdapter.ViewHolder> {
    private ArrayList<Train> trains = new ArrayList<>();
    private TrainsActivity activity;

    public TrainsAdapter(TrainsActivity activity, ArrayList<Train> trains) {
        this.trains = trains;
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.train_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.deptDayTextView.setText("Day: " + trains.get(position).getDeptDay());
        holder.deptTimeView.setText("Time: " + trains.get(position).getDeptTime());
        holder.originTextView.setText("Origin: " + trains.get(position).getOriginS());
        holder.destTextView.setText("Destination: " + trains.get(position).getDestinationS());
        holder.originTextView.setTag(trains.get(position).getOrigin());
        holder.destTextView.setTag(trains.get(position).getDestinationS());
        holder.trainIdView.setText("Train Id: " + trains.get(position).getTrainId());
        if (trains.get(position).getStatus() == 0) {
            holder.status.setBackgroundDrawable(new ColorDrawable(activity.getResources().getColor(R.color.md_yellow_700)));
            holder.status.setText("Pending");
            holder.trainRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(activity, CargoActivity.class);
                    intent.putExtra(Constants.TRAIN_ID, trains.get(position).getTrainId());
                    intent.putExtra(Constants.USER, activity.getUser());
                    activity.startActivity(intent);
                }
            });
        } else {
            holder.status.setBackgroundDrawable(new ColorDrawable(activity.getResources().getColor(R.color.md_green_700)));
            holder.status.setText("Departed");
            holder.trainRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(activity, MapsActivity.class);
                    CurrentLocation dest = trains.get(position).getCurLoc();
                    if (dest != null) {
                        Double lat = dest.getLat();
                        Double lng = dest.getLng();
                        LatLng latlng = new LatLng(lat, lng);
                        intent.putExtra("loc", latlng);
                        intent.putExtra(Constants.FROM, Constants.TRAINS_ADAPTER);
                        intent.putExtra(Constants.TRAIN_ID, trains.get(position).getTrainId());
                        activity.startActivity(intent);
                    }
                }
            });

        }
        if (trains.get(position).getCargos() != null) {
            holder.cargoesTextView.setText("Cargoes: " + trains.get(position).getCargos().size());
        } else {
            holder.cargoesTextView.setText("Cargoes: 0");
        }

    }

    @Override
    public int getItemCount() {
        return trains.size();
    }

    public void addData(ArrayList<Train> trains) {
        this.trains = trains;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView destTextView;
        TextView originTextView;
        TextView deptDayTextView;
        TextView deptTimeView;
        TextView trainIdView;
        TextView cargoesTextView;
        TextView status;
        LinearLayout trainRow;

        public ViewHolder(View itemView) {
            super(itemView);
            deptDayTextView = (TextView) itemView.findViewById(R.id.departure_day);
            destTextView = (TextView) itemView.findViewById(R.id.destination);
            deptTimeView = (TextView) itemView.findViewById(R.id.departure_time);
            originTextView = (TextView) itemView.findViewById(R.id.origin);
            trainIdView = (TextView) itemView.findViewById(R.id.train_id);
            cargoesTextView = (TextView) itemView.findViewById(R.id.cargoes_ctn);
            trainRow = (LinearLayout) itemView.findViewById(R.id.train_row);
            status = (TextView) itemView.findViewById(R.id.status);
        }
    }
}
