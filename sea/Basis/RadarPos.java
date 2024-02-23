package sea.Basis;

// Richtung umgebender Felder

//Alle enums bieten automatisch folgenden Methoden:
//String name() - Liefert der Enum-Wert als String
//int ordinal() - Liefert den Enum-Wert als Zahl
//EnumObj EnumName.valueOf(String name) - wandelt einen String in Enum-Wert um

public enum RadarPos {
	W, // West
	NW, // Nordwest
	N, // Nord
	NE, // Nordost
	E, // Ost
	SE, // Suedost
	S, // Sued
	SW // Suedwest
}
