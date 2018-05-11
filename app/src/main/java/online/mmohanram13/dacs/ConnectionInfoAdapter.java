package online.mmohanram13.dacs;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Mohan Ram M on 4/21/2018.
 */

public class ConnectionInfoAdapter extends RecyclerView.Adapter<ConnectionInfoAdapter.ConnectionViewHolder> {

    private List<ConnectionsActivity.Endpoint> connectionInfo;

    public class ConnectionViewHolder extends RecyclerView.ViewHolder{
        public TextView connInfo;

        public ConnectionViewHolder(View view){
            super(view);
            connInfo = view.findViewById(R.id.connInfo);
        }
    }

    public ConnectionInfoAdapter(List<ConnectionsActivity.Endpoint> connectionInfo){
        this.connectionInfo = connectionInfo;
    }

    @Override
    public ConnectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row, parent, false);

        return new ConnectionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ConnectionViewHolder holder, int position) {
        ConnectionsActivity.Endpoint connectionInformation = connectionInfo.get(position);
        holder.connInfo.setText(connectionInformation.getName());
    }

    @Override
    public int getItemCount() {
        return connectionInfo.size();
    }
}
