<template>
<v-app id="inspire">

    <v-navigation-drawer fixed app right
        v-model="drawerRight" 
        :clipped="$vuetify.breakpoint.lgAndUp" >

        <v-container grid-list-md text-xs-center>
            <v-layout row wrap >

                <v-flex xs12 v-for="card in cardModel" :key="card._links.self.href">
                    <v-card class="mx-auto" color="grey lighten-1" dark>
                        <v-card-title>
                            <span class="title font-weight-light">{{card.contentType}}</span>
                            <span class="font-weight-light">&nbsp;&nbsp;&nbsp;{{card.moment}}</span>
                        </v-card-title>

                        <v-card-text class="headline font-weight-bold">
                            {{card.filename}}
                        </v-card-text>

                        <v-card-actions>
                            <v-list-tile class="grow">
                                <v-layout
                                align-center
                                justify-end
                                >
                                    <v-btn small color="grey lighten-2" @click="download(card._links.self.href+'?flag=download', card.filename)">Download</v-btn>
                                    <v-btn small color="grey lighten-2" :href="card._links.self.href+'?flag=preview'" target="_blank" >Preview</v-btn>
                                </v-layout>
                            </v-list-tile>
                        </v-card-actions>
                    </v-card>
                </v-flex>

            </v-layout>
        </v-container>

    </v-navigation-drawer>


    <v-navigation-drawer fixed app
        v-model="drawer"
        :clipped="$vuetify.breakpoint.lgAndUp">
        
        <v-container fill-height>
            <v-layout justify-right>


                <kendo-hierarchicaldatasource 
                    ref="remoteDataSourceComponent"
                    :transport-read="onTreeRead"
                    :schema-model-id="'id'"
                ></kendo-hierarchicaldatasource>


                <kendo-treeview 
                    ref="treeview"
                    data-source-ref="remoteDataSourceComponent"
                    @expand="onTreeExpand"
                    @select="onTreeSelect"
                ></kendo-treeview>

            </v-layout>
            <!-- 
            <pre>{{selectedDirectory}}</pre> 
            -->

        </v-container>

    </v-navigation-drawer>


    <v-toolbar fixed app 
        :clipped-left="$vuetify.breakpoint.lgAndUp" 
        :clipped-right="$vuetify.breakpoint.lgAndUp">
        <v-toolbar-side-icon @click.stop="drawer = !drawer"></v-toolbar-side-icon>
        <v-toolbar-title>spring-data-rest-multipart-2.1.7.RELEASE</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-toolbar-side-icon @click.stop="drawerRight = !drawerRight"></v-toolbar-side-icon>
    </v-toolbar>


    <v-content id="content">
        <v-container fluid fill-height>
            <v-layout justify-center>
                <v-flex xs12>


                    <v-breadcrumbs :items="breadcrumbsModel" divider="/">
                        <template v-slot:item="props">
                            <a @click="onTreeSelect(props.item)">{{ props.item.filename }}</a>
                        </template>
                    </v-breadcrumbs>
                    

                    <kendo-grid ref="gridview" 
                        :data-source="gridModel"
                        :sortable="true"
                        :selectable="true"
                        @change="onGridSelect">

                        <kendo-grid-column :field="'filename'" :title="'Filename'" />
                        <kendo-grid-column :field="'contentType'" :title="'ContentType'" :width="200"/>
                        <kendo-grid-column :field="'bytes'" :title="'Size'" :width="100"/>
                        <kendo-grid-column :field="'moment'" :title="'lastModified'" :width="200"/>
                    </kendo-grid>
<!-- 
                    <b> 더보기 </b>
                    <pre>{{selectedFile}}</pre>
 -->                
                </v-flex>
            </v-layout>
        </v-container>


        <v-speed-dial  
            v-model="fab"

            :top="top"
            :right="right"
            :bottom="bottom"
            :left="left"

            :direction="direction"
            :open-on-hover="hover"
            :transition="transition">
            
            <template v-slot:activator>
                <v-btn fab color="blue darken-2" dark ref="more_vert">
                <v-icon>more_vert</v-icon>
                </v-btn>
            </template>
            <v-btn fab dark color="green" @click="addFolder(true)">
                <v-icon>create_new_folder</v-icon>
            </v-btn>

            <v-btn fab dark color="indigo" type="file" @click="addFile(null)">
                <v-icon>cloud_upload</v-icon>
            </v-btn>

            <input
                type="file"
                style="display: none"
                ref="fileupload"
                @change="addFile"
            />

            <v-btn fab dark color="red" @click="remove">
                <v-icon :disabled="false">delete</v-icon>
            </v-btn>
        </v-speed-dial> 

        <v-dialog v-model="dialog" persistent max-width="400px">
            <v-card>
                <v-card-text>
                    <v-text-field label="Directory*" v-model="dialogText" required></v-text-field>
                </v-card-text>
                <v-card-actions>
                    <v-btn color="green" @click="addFolder(false)">Create</v-btn>
                    <v-btn color="green" @click="dialog = false">Cancel</v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>

    </v-content>
</v-app>  
</template>

<script>

const filesize = require("file-size");

export default {
    name : "Multipart",
    components: {
    },
    data: () => ({
        drawer: true,
        drawerRight: false,

        direction: 'bottom',
        fab: false,
        fling: false,
        hover: true,
        tabs: null,
        top: true,
        bottom: false,
        right: true,
        left: false,
        transition: 'slide-y-transition' ,

        dialog: false,
        dialogText: null,

        treeModel: [

        ],
       
        breadcrumbsModel :[
        ],

        gridModel : [
        ],

        cardModel :[
        ],

        
        selectedDirectory : null,
        selectedFile : null,
    }),

    methods : {

        ///////////////////////////////////////////////////////
        //
        ///////////////////////////////////////////////////////
        onTreeRead(options){

            var path = options.data.id;


            if (path == undefined) {
                this.$log.debug(this.$options.name, 'onTreeRead');
                options.success(this.treeModel);
            }else{

                var url = path+"?flag=childs&sort=filename&size=50&sort=directory";
                this.$log.debug(this.$options.name, 'onTreeRead', url);

                this.$axios({
                    method:  'get',
                    url: url,
                }).then((result) => {
                    this.$log.debug(this.$options.name, 'onTreeRead', 'success');
                    var data = result.data._embedded.multiparts;

                    var items = [];
                    for(var i=0; i < data.length; i++){
                        if('text/directory' == data[i].contentType){
                            data[i].text = data[i].filename;
                            data[i].parentId = path;
                            data[i].id = data[i]._links.self.href;
                            data[i].hasChildren = true;
                            items.push(data[i]);
                        }
                    }
                    options.success(items);

                }).catch((error) => {
                    alert('multipart repository is not found. Please check your http url. onTreeRead() '+url);
                    this.$log.debug(this.$options.name, 'onTreeRead', error);
                });
            } 
        },

        onTreeSelect(ev) {
            var treeview = this.$refs.treeview.kendoWidget();

            if(ev == null){
                this.selectedDirectory = treeview.dataSource.at(0);
                var rootEle = treeview.findByUid(this.selectedDirectory.uid);
                treeview.select(rootEle);

            }else if(ev.node == null){
                this.selectedDirectory = ev;
                var selectedEle = treeview.findByUid(ev.uid);
                treeview.select(selectedEle);

            }else{
                var uid = ev.node.dataset.uid;
                this.selectedDirectory = ev.sender.dataSource.getByUid(uid);
            }

            this.$log.debug(this.$options.name, 'onTreeSelect', this.selectedDirectory);
            
            this.drawerRight = false;
            this.selectedFile = null;
            this.onBreadcrumbsChange();
            this.onGridChange();
        },
        
        onTreeExpand(e) {
            this.$log.debug(this.$options.name, 'onTreeExpand');
            var treeview = this.$refs.treeview.kendoWidget();
            var dataItem = treeview.dataItem(e.node);
            dataItem.loaded(false);
        },


        onTreeChange(){
            this.$log.debug(this.$options.name, 'onTreeChange');

            var treeview = this.$refs.treeview.kendoWidget();
            setTimeout(()=>{
                var selectedEle = treeview.findByUid(this.selectedDirectory.uid);
                treeview.collapse(selectedEle);
                setTimeout(()=>{
                    treeview.expand(selectedEle);
                }, 200)
            }, 100)
        },

        ///////////////////////////////////////////////////////
        //
        ///////////////////////////////////////////////////////
        onBreadcrumbsChange(node, array){
            if(array == undefined){
                var temp = [];
                this.onBreadcrumbsChange(this.selectedDirectory, temp);
                this.breadcrumbsModel = temp;
                this.$log.debug(this.$options.name, 'onBreadcrumbsChange');
            }else{
                array.unshift(node);
                if(node.parentId){
                    var treeview = this.$refs.treeview.kendoWidget();
                    var pNode = treeview.dataSource.get(node.parentId);
                    if(pNode != null)
                        this.onBreadcrumbsChange(pNode, array);
                }
            }
        },

        ///////////////////////////////////////////////////////
        //
        ///////////////////////////////////////////////////////
        onGridChange(newItem, page){

            var path = this.selectedDirectory.id;

            var url = path+"?flag=childs&sort=filename&page="+(page ? page : 0);

            this.$log.debug(this.$options.name, 'onGridChange', url);
            this.$axios({
                method:  'get',
                url: url,

            }).then((result) => {
                this.$log.debug(this.$options.name, 'onGridChange', 'success');
                var data = result.data._embedded.multiparts;

                var selectedIndex = 0;

                if(page == 0){
                    this.gridModel.splice(0, this.gridModel.length);
                }else{
                    this.gridModel.splice(this.gridModel.length-1, 1);
                }
                for(var i=0; i < data.length; i++){
                    data[i].bytes = filesize(data[i].size).human('jedec');
                    data[i].id = data[i]._links.self.href;
                    // data[i].moment = this.$moment(data[i].lastModified).fromNow();
                    data[i].moment = this.$moment(data[i].lastModified).format("YYYY-MM-DD HH:mm:ss");
                    if(newItem && newItem._links.self.href == data[i]._links.self.href){
                        selectedIndex = this.gridModel.length;
                    }
                    this.gridModel.push(data[i]);
                }
                if(result.data._links.next){
                    var more = {'contentType' : 'more', 'page' : (page ? page+1 : 1)}
                    this.gridModel.push(more);
                }

                if(newItem){
                    setTimeout(()=>{
                        var gridview = this.$refs.gridview.kendoWidget();
                        gridview.select("tr:eq("+selectedIndex+")");
                    }, 300);
                }

            }).catch((error) => {
                alert('Multipart repository is not found. Please check your http url. onGridChange() '+url);
                this.$log.debug(this.$options.name, 'onGridChange', error);
            });
        },

        onGridSelect(ev){

            var uid = ev.sender.select();
            this.selectedFile = ev.sender.dataItem(uid);

            if("more" == this.selectedFile.contentType){
                this.drawerRight = false;
                this.cardModel.splice(0, this.cardModel.length);
                this.onGridChange(null, this.selectedFile.page);


            }else if("text/directory" == this.selectedFile.contentType){
                this.drawerRight = false;
                this.cardModel.splice(0, this.cardModel.length);

                //????????                

            }else{
                this.drawerRight = true;
                
                var url =  this.selectedFile.id;
                
                this.$log.debug(this.$options.name, 'onGridSelect', url);
                this.$axios({
                    method:  'get',
                    url: url,
                    // url: path+"?flag=history&sort=lastModified,desc",

                }).then((result) => {
                    this.$log.debug(this.$options.name, 'onGridSelect', 'success');  

                    var data = result.data;
                    data.moment = data.lastModified;

                    this.cardModel.splice(0, this.cardModel.length);
                    this.cardModel.push(data);

                    // var data = result.data._embedded.multiparts;
                    // this.cardModel.splice(0, this.cardModel.length);
                    // for(var i=0; i < data.length; i++){
                    //     data[i].moment = data[i].lastModified;
                    //     // data[i].moment = this.$moment(data[i].lastModified).format("YYYY-MM-DD HH:mm:ss");
                    //     // data[i].moment = this.$moment(data[i].lastModified).fromNow();
                    //     this.cardModel.push(data[i]);
                    // }

                }).catch((error) => {
                    alert('Multipart repository is not found. Please check your http url. onGridSelect() '+url);
                    this.$log.debug(this.$options.name, 'onGridSelect', error);
                });
            }

        },

        ///////////////////////////////////////////////////////
        //
        ///////////////////////////////////////////////////////
        addFolder(flag){
            this.dialog = flag;
            if(this.dialog == false && this.dialogText.length > 0){

                this.$log.debug(this.$options.name, 'addFolder', this.dialogText);

                let formData = new FormData();
                formData.append('directory', this.dialogText);
                this.upload(formData);
            }
        },
        addFile(e){
            if(e == null) {
                this.$refs.fileupload.click();
            }else{
                var files = e.target.files;
                if(files[0] !== undefined) {

                    this.$log.debug(this.$options.name, 'addFile', e);

                    let formData = new FormData();
                    formData.append('file', files[0]);
                    this.upload(formData);
                }
            }
        },

        upload(formData){


            var url = this.selectedDirectory.id;
            this.$log.debug(this.$options.name, 'upload', url);

            this.$axios({
                method:  'post',
                headers: {'Content-Type': 'multipart/form-data'},
                url: url,
                data: formData,

            }).then((response) => {
                this.$log.debug(this.$options.name, 'upload', 'success');
                this.onTreeChange();
                this.onGridChange(response.data);

            }).catch((error) => {
                alert('Multipart repository is not found. Please check your http url. upload() '+url);
                this.$log.debug(this.$options.name, 'upload', error);
            })
        },

        remove(){
            if(this.selectedFile == null) return;
            
            var url = this.selectedFile.id;
            this.$log.debug(this.$options.name, 'remove', url);

            this.$axios({
                method:  'delete',
                url: url

            }).then(() => {
                this.$log.debug(this.$options.name, 'remove', 'success');
                this.onTreeChange();
                this.onGridChange();

            }).catch((error) => {
                alert('Multipart repository is not found. Please check your http url. remove() '+url);
                this.$log.debug(this.$options.name, 'remove', error);
            })
        },

        download(href, download) {
            this.$log.debug(this.$options.name, "download", href, download);

            const a = document.createElement('a');
            a.href = href;
            a.download = download;
            a.style.display = 'none';
            document.body.appendChild(a);
            a.click();
            setTimeout(() => {
                document.body.removeChild(a);
                window.URL.revokeObjectURL(href);
            }, 100);
        }
    },
    created: function() {
        //this.$log.debug(this.$options.name, 'created');
        this.treeModel = [{
            id : '/multipart/'+this.$route.params.backend,
            //id : 'http://localhost:19085/multipart/'+this.$route.params.backend,
            text : "Root Storage",
            filename : 'Root Storage',
            hasChildren : true
        }];
    },
    mounted: function() {
        // this.$log.debug(this.$options.name, 'mounted');
        this.onTreeSelect();
    },
    updated: function() {
        //this.$log.debug(this.$options.name, 'updated');
    },
    destroyed: function() {
        //this.$log.debug(this.$options.name, 'destroyed');
    },
}
</script>

<style>
  #content .v-speed-dial {
    position: absolute;
  }

  #content .v-btn--floating {
    position: relative;
  }
</style>
