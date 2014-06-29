package com.martinongtangco.contactmanager.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.ImageView;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private final int EDIT_MODE = 0, DELETE_MODE = 1;

    private ArrayAdapter<Contact> _contactAdapter;
    private EditText _nameTxt, _phoneTxt, _emailTxt, _addressTxt;
    private ImageView _contactImageImgView;
    private List<Contact> Contacts = new ArrayList<Contact>();
    private ListView _contactListView;
    private Uri _imageURI = Uri.parse("android.resource://com.martinongtangco.contactmanager.app/res/drawable/nouserimg.png");
    private DatabaseHandler _dbHandler;
    private int _longClickedItemIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _dbHandler = new DatabaseHandler(getApplicationContext());
        _nameTxt = (EditText) findViewById(R.id.txtName);
        _phoneTxt = (EditText) findViewById(R.id.txtPhone);
        _emailTxt = (EditText) findViewById(R.id.txtEmail);
        _addressTxt = (EditText) findViewById(R.id.txtAddress);
        _contactImageImgView = (ImageView) findViewById(R.id.imgViewContactImage);


        _contactListView = (ListView) findViewById(R.id.listView);
        registerForContextMenu(_contactListView);
        _contactListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                _longClickedItemIndex = position;
                return false;
            }
        });

        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("creator");
        tabSpec.setContent(R.id.tabCreator);
        tabSpec.setIndicator("Creator");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("list");
        tabSpec.setContent(R.id.tabContactList);
        tabSpec.setIndicator("List");
        tabHost.addTab(tabSpec);

        final Button addBtn = (Button) findViewById(R.id.btnAdd);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewContact();
            }
        });

        _nameTxt.addTextChangedListener(getTextWatcher(addBtn));

        _contactImageImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, "Select Contact image"), 1);
            }
        });

        if (_dbHandler.getContactsCount() > 0)
            Contacts.addAll(_dbHandler.getAllContacts());

        // always populate
        populateList();
    }

    private void addNewContact() {
        int newId = _dbHandler.getContactsCount() + 1;
        String message = "";

        Contact contact = new Contact(newId,
                                      String.valueOf(_nameTxt.getText()),
                                      String.valueOf(_phoneTxt.getText()),
                                      String.valueOf(_emailTxt.getText()),
                                      String.valueOf(_addressTxt.getText()),
                                      _imageURI);
        if (!contactExists(contact)) {
            _dbHandler.createContact(contact);
            Contacts.add(contact);
            _contactAdapter.notifyDataSetChanged();

            message = String.valueOf(_nameTxt.getText()) + " has been added to your Contacts!";

            // clear text
            _nameTxt.setText("");
            _phoneTxt.setText("");
            _emailTxt.setText("");
            _addressTxt.setText("");

        } else {
            message = String.valueOf(_nameTxt.getText()) + " already exists!";
        }

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        menu.setHeaderIcon(R.drawable.ic_action_edit);
        menu.setHeaderTitle("Options");
        menu.add(Menu.NONE, EDIT_MODE, menu.NONE, "Edit");
        menu.add(Menu.NONE, DELETE_MODE, menu.NONE, "Delete");
    }

    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case EDIT_MODE:

                break;
            case DELETE_MODE:
                _dbHandler.deleteContact(Contacts.get(_longClickedItemIndex));
                Contacts.remove(_longClickedItemIndex);
                _contactAdapter.notifyDataSetChanged();
                break;
        }

        return super.onContextItemSelected(item);
    }

    private boolean contactExists(Contact contact) {
        boolean result = false;
        String name = contact.getName();
        int contactCount = Contacts.size();

        for (int i = 0; i < contactCount; i ++) {
            if (name.compareToIgnoreCase(Contacts.get(i).getName()) == 0) {
                result = true;
                break;
            }
        }

        return result;
    }

    public void onActivityResult(int reqCode, int resCode, Intent data) {
        if (resCode == RESULT_OK) {
            if (reqCode == 1) {
                _imageURI = data.getData();
                _contactImageImgView.setImageURI(data.getData());
            }
        }
    }

    private void populateList() {
        _contactAdapter = new ContactListAdapter();
        _contactListView.setAdapter(_contactAdapter);
    }

    // nameText events
    private TextWatcher getTextWatcher(final Button addBtn) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // enable button when name is populated
                addBtn.setEnabled(!String.valueOf(_nameTxt.getText()).trim().isEmpty());
            }

            @Override
            public void afterTextChanged(android.text.Editable editable) {

            }
        };
    }

    private class ContactListAdapter extends ArrayAdapter<Contact> {
        public ContactListAdapter() {
            super (MainActivity.this, R.layout.listview_item, Contacts);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null)
                view = getLayoutInflater().inflate(R.layout.listview_item, parent, false);

            Contact currentContact = Contacts.get(position);

            TextView name = (TextView) view.findViewById(R.id.contactName);
            name.setText(currentContact.getName());

            TextView phone = (TextView) view.findViewById(R.id.phoneNumber);
            phone.setText(currentContact.getPhone());

            TextView email = (TextView) view.findViewById(R.id.emailAddress);
            email.setText(currentContact.getEmail());

            TextView address = (TextView) view.findViewById(R.id.cAddress);
            address.setText(currentContact.getAddress());

            ImageView ivContactImage = (ImageView) view.findViewById(R.id.ivContactImage);
            ivContactImage.setImageURI(currentContact.getImageURI());

            return view;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
