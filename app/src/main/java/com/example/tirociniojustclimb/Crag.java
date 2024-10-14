package com.example.tirociniojustclimb;


import java.util.ArrayList;

public class Crag{
    ArrayList<Sector> sectors;
    String name;
    String description;
    String latitude;
    String longitude;
    String counterDelete;

    public Crag(){
        sectors = new ArrayList<Sector>();
        name = "";
        description = "";
        latitude = "";
        longitude = "";
        counterDelete = "0";
    }

    public Crag(String _name,String _description,String _lat,String _lon){
        sectors = new ArrayList<Sector>();
        name = _name;
        description = _description;
        latitude = _lat;
        longitude = _lon;
        counterDelete ="0";
    }
    public Crag(String _name,String _description,String _lat,String _lon, String _counter){
        sectors = new ArrayList<Sector>();
        name = _name;
        description = _description;
        latitude = _lat;
        longitude = _lon;
        counterDelete = _counter;
    }

    public void add_sector(Sector sect){
        sectors.add(sect);
    }

    public void add_sectors(ArrayList<Sector> list){
        for(Sector sect : list){
            this.add_sector(sect);
        }
    }

    public void setCounterDelete(String del){
        this.counterDelete = del;
    }
    public String getCounterDelete (){
        return counterDelete;
    }
    public void incrementaDelete(){
        int n = Integer.parseInt(this.counterDelete);
        n = n +1;
        this.counterDelete = Integer.toString(n);
    }

    public ArrayList<Sector> getSectors(){
        return sectors;
    }
}

class Sector {
    ArrayList<Route> routes;
    String name;
    String id_sector;
    String counterDelete;

    public Sector(){
        routes = new ArrayList<Route>();
        name = "";
        counterDelete ="0";
    }

    public Sector(String _name){
        routes = new ArrayList<Route>();
        name = _name;
        counterDelete = "0";
    }
    public Sector(String _name, String _delete){
        routes = new ArrayList<Route>();
        name = _name;
        counterDelete = _delete;
    }

    public ArrayList<Route> getRoutes(){
        return routes;
    }

    public void add_route(Route route){
        routes.add(route);
    }

    public void setName(String n){ this.name = n; }
    public String getName(){ return name; }

    public void add_routes(ArrayList<Route> list){
        for(Route route : list){
            this.add_route(route);
        }
    }
    public void setId_Sector(String id){
        this.id_sector = id;
    }
    public String getId_Sector(){
        return id_sector;
    }

    public void setCounterDeleteSector(String del){
        this.counterDelete = del;
    }
    public String getCounterDeleteSector (){
        return counterDelete;
    }
    public void incrementaDelete(){
        int n = Integer.parseInt(this.counterDelete);
        n = n +1;
        this.counterDelete = Integer.toString(n);
    }
}

class Route {
    String name;
    String grade;
    String lenght;
    String id_route;
    String ngrade;
    ArrayList<Note> notes;
    String deleteN;

    public Route(){
        name = "";
        grade = "";
        lenght = "0";
        ngrade = "0";
        deleteN = "0";
        notes = new ArrayList<Note>();
    }

    public Route(String _name,String _grade,String _lenght){
        name = _name;
        grade = _grade;
        lenght = _lenght;
        deleteN ="0";
        notes = new ArrayList<Note>();
    }

    public void setPar ( String nome, String grado, String lunghezza, String _dele){
        this.name = nome;
        this.grade = grado;
        this.lenght = lunghezza;
        this.deleteN = _dele;
    }

    public void add_note (Note note){
        notes.add(note);
    }
    public ArrayList<Note> getNotes(){ return notes;}

    public void setDeleteN (String n){ this.deleteN = n;}
    public String getDeleteN (){ return this.deleteN;}
    public void incrementaDelete(){
        int n = Integer.parseInt(this.deleteN);
        n = n +1;
        this.deleteN = Integer.toString(n);
    }

    public void setId_route(String id){
        this.id_route = id;
    }
    public String getId_route(){
        return id_route;
    }

    public String getName(){
        return this.name;
    }
    public String getGrade(){
        return this.grade;
    }
    public String getLenght(){
        return this.lenght;
    }



}


class Note implements Comparable<Note>{
    String txtNota;
    String data;
    String id;
    String id_user;

    public Note(){
        txtNota = "";
    }

    public Note(String d, String txt){
        this.data = d;
        this.txtNota = txt;
    }

    public void setId_Note(String id){
        this.id = id;
    }
    public String getId_note(){
        return id;
    }

    public void setId_user(String id){
        this.id_user = id;
    }
    public String getId_user(){
        return id_user;
    }

    @Override
    public int compareTo(Note n) {
        if (data == null || n.data == null) {
            return 0;
        }
        return data.compareTo(n.data);
    }

}

class Grade {
    String grado;
    String id_user;
    String id;

    public Grade() {
        grado = "";
    }

    public Grade(String g, String id) {
        this.grado = g;
        this.id_user = id;
    }

    public void setId_user(String id) {
        this.id_user = id;
    }
    public String getId_user() {
        return id_user;
    }

    public void setGrado(String g) {
        this.grado = g;
    }

    public String getGrado() {
        return grado;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }


}
