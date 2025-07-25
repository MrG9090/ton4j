package org.ton.ton4j.smartcontract.dns;

import static java.util.Objects.isNull;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;
import lombok.Builder;
import lombok.Getter;
import org.ton.java.adnl.AdnlLiteClient;
import org.ton.ton4j.address.Address;
import org.ton.ton4j.cell.Cell;
import org.ton.ton4j.cell.CellBuilder;
import org.ton.ton4j.smartcontract.token.nft.NftUtils;
import org.ton.ton4j.smartcontract.types.CollectionData;
import org.ton.ton4j.smartcontract.types.ItemData;
import org.ton.ton4j.smartcontract.types.WalletCodes;
import org.ton.ton4j.smartcontract.wallet.Contract;
import org.ton.ton4j.tonlib.Tonlib;
import org.ton.ton4j.tonlib.types.RunResult;
import org.ton.ton4j.tonlib.types.TvmStackEntryCell;
import org.ton.ton4j.tonlib.types.TvmStackEntryNumber;
import org.ton.ton4j.tonlib.types.TvmStackEntrySlice;
import org.ton.ton4j.utils.Utils;

/**
 * <a
 * href="https://github.com/ton-blockchain/dns-contract/blob/main/func/nft-collection.fc">nft-collection</a>
 */
@Builder
@Getter
public class DnsCollection implements Contract {

  String dnsItemCodeHex;
  Cell collectionContent;
  Cell code;

  private Tonlib tonlib;
  private long wc;

  private AdnlLiteClient adnlLiteClient;

  @Override
  public AdnlLiteClient getAdnlLiteClient() {
    return adnlLiteClient;
  }

  @Override
  public void setAdnlLiteClient(AdnlLiteClient pAdnlLiteClient) {
    adnlLiteClient = pAdnlLiteClient;
  }

  @Override
  public Tonlib getTonlib() {
    return tonlib;
  }

  @Override
  public void setTonlib(Tonlib pTonlib) {
    tonlib = pTonlib;
  }

  @Override
  public long getWorkchain() {
    return wc;
  }

  /** Options collectionContent: Cell dnsItemCodeHex String address: Address String */
  public static class DnsCollectionBuilder {}

  public static DnsCollectionBuilder builder() {
    return new CustomDnsCollectionBuilder();
  }

  private static class CustomDnsCollectionBuilder extends DnsCollectionBuilder {
    @Override
    public DnsCollection build() {
      if (isNull(super.collectionContent)) {
        throw new Error("Required collectionContent cell");
      }

      if (isNull(super.dnsItemCodeHex)) {
        throw new Error("Required dnsItemCodeHex field");
      }
      return super.build();
    }
  }

  public String getName() {
    return "dnsCollection";
  }

  /**
   * @return Cell cell contains dns collection data
   */
  @Override
  public Cell createDataCell() {
    return CellBuilder.beginCell()
        .storeRef(collectionContent)
        .storeRef(CellBuilder.beginCell().fromBoc(dnsItemCodeHex).endCell())
        .endCell();
  }

  @Override
  public Cell createCodeCell() {
    return CellBuilder.beginCell().fromBoc(WalletCodes.dnsCollection.getValue()).endCell();
  }

  /**
   * @return CollectionInfo
   */
  public static CollectionData getCollectionData(Tonlib tonlib, Address dnsCollectionAddress) {
    // todo
    RunResult result = tonlib.runMethod(dnsCollectionAddress, "get_collection_data");

    if (result.getExit_code() != 0) {
      throw new Error("method get_collection_data, returned an exit code " + result.getExit_code());
    }

    TvmStackEntryNumber nextItemIndexResult = (TvmStackEntryNumber) result.getStack().get(0);
    long nextItemIndex = nextItemIndexResult.getNumber().longValue();

    TvmStackEntryCell collectionContentResult =
        (TvmStackEntryCell) result.getStack().get(1); // cell or slice
    Cell collectionContent =
        CellBuilder.beginCell()
            .fromBoc(Utils.base64ToBytes(collectionContentResult.getCell().getBytes()))
            .endCell();
    String collectionContentUri = NftUtils.parseOffChainUriCell(collectionContent);

    return CollectionData.builder()
        .collectionContentUri(collectionContentUri)
        .collectionContentCell(collectionContent)
        .ownerAddress(null)
        .nextItemIndex(nextItemIndex) // always -1
        .build();
  }

  public static ItemData getNftItemContent(Tonlib tonlib, Address dnsItemAddress) {
    return DnsItem.getData(tonlib, dnsItemAddress);
  }

  /**
   * @param index BigInteger
   * @return Address
   */
  public static Address getNftItemAddressByIndex(
      Tonlib tonlib, Address collectionAddress, BigInteger index) {
    // todo
    Deque<String> stack = new ArrayDeque<>();

    stack.offer("[num, " + index.toString() + "]");
    RunResult result = tonlib.runMethod(collectionAddress, "get_nft_address_by_index", stack);

    if (result.getExit_code() != 0) {
      throw new Error(
          "method get_nft_address_by_index, returned an exit code " + result.getExit_code());
    }

    TvmStackEntrySlice addr = (TvmStackEntrySlice) result.getStack().get(0);
    return NftUtils.parseAddress(
        CellBuilder.beginCell().fromBoc(Utils.base64ToBytes(addr.getSlice().getBytes())).endCell());
  }

  public static Address getNftItemAddressByDomain(
      Tonlib tonlib, Address dnsCollectionAddress, String domain) {
    // todo
    Cell cell = CellBuilder.beginCell().storeString(domain).endCell();
    String cellHash = Utils.bytesToHex(cell.hash());
    return getNftItemAddressByIndex(tonlib, dnsCollectionAddress, new BigInteger(cellHash, 16));
  }

  /**
   * @param domain String e.g "sub.alice.ton"
   * @param category String category of requested DNS record, null for all categories
   * @param oneStep boolean non-recursive
   * @return Cell | Address | AdnlAddress | null
   */
  public static Object resolve(
      Tonlib tonlib,
      Address dnsCollectionAddress,
      String domain,
      String category,
      boolean oneStep) {
    return DnsUtils.dnsResolve(tonlib, dnsCollectionAddress, domain, category, oneStep);
  }
}
