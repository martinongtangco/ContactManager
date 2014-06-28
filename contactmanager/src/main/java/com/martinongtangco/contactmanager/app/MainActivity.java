package com.martinongtangco.contactmanager.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

    private EditText _nameTxt, _phoneTxt, _emailTxt, _addressTxt;
    private ImageView _contactImageImgView;
    private List<Contact> Contacts = new ArrayList<Contact>();
    private ListView _contactListView;
    private Uri _imageURI = Uri.parse("android.resource://com.martinongtangco.contactmanager.app/res/drawable/nouserimage.png");
    private DatabaseHandler _dbHandler;

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

                    message = String.valueOf(_nameTxt.getText()) + " has been added to your Contacts!";
                } else {
                    message = String.valueOf(_nameTxt.getText()) + " already exists!";
                }

                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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
        ArrayAdapter<Contact> adapter = new ContactListAdapter();
        _contactListView.setAdapter(adapter);
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
