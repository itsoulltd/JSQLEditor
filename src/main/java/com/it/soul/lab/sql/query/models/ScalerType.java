package com.it.soul.lab.sql.query.models;

public enum ScalerType{
	COUNT,
	MAX,
	MIN,
	SUM,
	AVG;
	
	public String toString(){
		String result;
		switch (this) {
		case MAX:
			result = "MAX";
			break;
		case MIN:
			result = "MIN";
			break;
		case SUM:
			result = "SUM";
			break;
		case AVG:
			result = "AVG";
			break;
		default:
			result = "COUNT";
			break;
		}
		return result;
	}
	
	public String toString(String column){
		String result = this.toString()+"("+column+")";
		return result;
	}
	
	public String toAlias(String column){
		String result = this.toString(column)+" AS " + this.toString().toLowerCase()+"_"+column;
		return result;
	}
	
}